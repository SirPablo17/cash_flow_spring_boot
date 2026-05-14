package pablo.nasc.cash_flow_spring_boot.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private static final Set<String> IDEMPOTENT_METHODS =
            Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final Duration CLEANUP_EVERY = Duration.ofMinutes(10);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();
    private volatile Instant lastCleanup = Instant.now();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !IDEMPOTENT_METHODS.contains(request.getMethod())
                || !StringUtils.hasText(request.getHeader(IDEMPOTENCY_KEY_HEADER));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        cleanupExpiredEntries();

        byte[] requestBody = request.getInputStream().readAllBytes();
        String requestHash = sha256(requestBody);
        String cacheKey = buildCacheKey(request);
        CachedBodyRequest wrappedRequest = new CachedBodyRequest(request, requestBody);
        Object lock = locks.computeIfAbsent(cacheKey, ignored -> new Object());

        try {
            synchronized (lock) {
                CachedResponse cached = cache.get(cacheKey);

                if (cached != null) {
                    if (!cached.requestHash().equals(requestHash)) {
                        writeConflict(response, request);
                        return;
                    }

                    replay(response, cached);
                    return;
                }

                ContentCachingResponseWrapper wrappedResponse =
                        new ContentCachingResponseWrapper(response);

                filterChain.doFilter(wrappedRequest, wrappedResponse);

                if (isCacheable(wrappedResponse.getStatus())) {
                    cache.put(cacheKey, CachedResponse.from(wrappedResponse, requestHash));
                }

                wrappedResponse.copyBodyToResponse();
            }
        } finally {
            locks.remove(cacheKey, lock);
        }
    }

    private String buildCacheKey(HttpServletRequest request) {
        String query = request.getQueryString();
        String target = query == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + query;

        return request.getMethod()
                + ":" + target
                + ":" + fingerprint(request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER))
                + ":" + fingerprint(request.getHeader(HttpHeaders.AUTHORIZATION))
                + ":" + request.getHeader(IDEMPOTENCY_KEY_HEADER);
    }

    private boolean isCacheable(int status) {
        return status >= 200 && status < 300;
    }

    private void replay(HttpServletResponse response, CachedResponse cached) throws IOException {
        response.setStatus(cached.status());

        if (cached.contentType() != null) {
            response.setContentType(cached.contentType());
        }

        if (cached.characterEncoding() != null) {
            response.setCharacterEncoding(cached.characterEncoding());
        }

        cached.headers().forEach((name, values) ->
                values.forEach(value -> response.addHeader(name, value))
        );

        response.addHeader("Idempotency-Replayed", "true");
        response.getOutputStream().write(cached.body());
    }

    private void writeConflict(HttpServletResponse response,
                               HttpServletRequest request) throws IOException {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", "A chave de idempotencia ja foi usada com outro corpo de requisicao.",
                "path", request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void cleanupExpiredEntries() {
        Instant now = Instant.now();
        if (Duration.between(lastCleanup, now).compareTo(CLEANUP_EVERY) < 0) {
            return;
        }

        lastCleanup = now;
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private String fingerprint(String value) {
        return StringUtils.hasText(value) ? sha256(value.getBytes(StandardCharsets.UTF_8)) : "none";
    }

    private String sha256(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value);
            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 nao esta disponivel.", ex);
        }
    }

    private record CachedResponse(
            int status,
            String contentType,
            String characterEncoding,
            Map<String, List<String>> headers,
            byte[] body,
            String requestHash,
            Instant createdAt
    ) {

        private static CachedResponse from(ContentCachingResponseWrapper response,
                                           String requestHash) {
            Map<String, List<String>> headers = new LinkedHashMap<>();

            for (String headerName : response.getHeaderNames()) {
                if (shouldReplayHeader(headerName)) {
                    headers.put(headerName, new ArrayList<>(response.getHeaders(headerName)));
                }
            }

            return new CachedResponse(
                    response.getStatus(),
                    response.getContentType(),
                    response.getCharacterEncoding(),
                    headers,
                    response.getContentAsByteArray(),
                    requestHash,
                    Instant.now()
            );
        }

        private static boolean shouldReplayHeader(String headerName) {
            return !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(headerName)
                    && !HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(headerName)
                    && !HttpHeaders.CONNECTION.equalsIgnoreCase(headerName);
        }

        private boolean isExpired(Instant now) {
            return Duration.between(createdAt, now).compareTo(CACHE_TTL) > 0;
        }
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(body);

            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int read() {
                    return inputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(
                    new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)
            );
        }
    }
}
