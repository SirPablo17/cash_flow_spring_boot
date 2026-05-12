package pablo.nasc.cash_flow_spring_boot.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting baseado em IP usando o algoritmo Token Bucket (Bucket4j 8.x).
 *
 * Regras:
 *   - /auth/** → 10 requisições por minuto (proteção contra força bruta)
 *   - demais   → 60 requisições por minuto (uso normal)
 *
 * Headers retornados no 429:
 *   X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, Retry-After
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int      AUTH_LIMIT    = 10;
    private static final Duration AUTH_REFILL   = Duration.ofMinutes(1);

    private static final int      GENERAL_LIMIT  = 60;
    private static final Duration GENERAL_REFILL = Duration.ofMinutes(1);
    private static final Duration BUCKET_TTL     = Duration.ofMinutes(15);
    private static final Duration CLEANUP_EVERY  = Duration.ofMinutes(5);

    private final Map<String, ClientBucket> authBuckets    = new ConcurrentHashMap<>();
    private final Map<String, ClientBucket> generalBuckets = new ConcurrentHashMap<>();
    private volatile Instant lastCleanup = Instant.now();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String  ip             = extractClientIp(request);
        boolean isAuthEndpoint = request.getRequestURI().contains("/api/v1/auth");

        cleanupExpiredBuckets();

        ClientBucket clientBucket = isAuthEndpoint
                ? authBuckets.computeIfAbsent(ip,    k -> new ClientBucket(buildBucket(AUTH_LIMIT,    AUTH_REFILL)))
                : generalBuckets.computeIfAbsent(ip, k -> new ClientBucket(buildBucket(GENERAL_LIMIT, GENERAL_REFILL)));

        clientBucket.touch();
        Bucket bucket = clientBucket.bucket();
        long availableTokens = bucket.getAvailableTokens();
        int  limit           = isAuthEndpoint ? AUTH_LIMIT : GENERAL_LIMIT;
        long refillSeconds   = isAuthEndpoint ? AUTH_REFILL.toSeconds() : GENERAL_REFILL.toSeconds();

        if (bucket.tryConsume(1)) {
            response.addHeader("X-RateLimit-Limit",     String.valueOf(limit));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(availableTokens - 1));
            chain.doFilter(request, response);
        } else {
            log.warn("[RateLimit] IP {} bloqueado em {} — limite de {} req/min atingido",
                    ip, request.getRequestURI(), limit);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-RateLimit-Limit",     String.valueOf(limit));
            response.addHeader("X-RateLimit-Remaining", "0");
            response.addHeader("X-RateLimit-Reset",     String.valueOf(refillSeconds));
            response.addHeader("Retry-After",            String.valueOf(refillSeconds));

            var body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "status",    429,
                    "error",     "Too Many Requests",
                    "message",   "Limite de requisições atingido. Tente novamente em "
                            + refillSeconds + " segundos.",
                    "path",      request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    /**
     * Bucket4j 8.x — nova API:
     *   Bandwidth.builder()
     *     .capacity(n)
     *     .refillGreedy(n, duration)  → reabastece gradualmente
     *     .build()
     */
    private Bucket buildBucket(int capacity, Duration refillPeriod) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillPeriod)
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Extrai o IP real do cliente respeitando proxies e load balancers.
     */
    private String extractClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");

        if (isTrustedProxy(remoteAddr) && forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return remoteAddr;
    }

    private boolean isTrustedProxy(String remoteAddr) {
        return remoteAddr != null && (
                remoteAddr.equals("127.0.0.1")
                        || remoteAddr.equals("0:0:0:0:0:0:0:1")
                        || remoteAddr.equals("::1")
                        || remoteAddr.startsWith("10.")
                        || remoteAddr.startsWith("192.168.")
                        || remoteAddr.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*")
        );
    }

    private void cleanupExpiredBuckets() {
        Instant now = Instant.now();
        if (Duration.between(lastCleanup, now).compareTo(CLEANUP_EVERY) < 0) {
            return;
        }

        lastCleanup = now;
        authBuckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        generalBuckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private static final class ClientBucket {

        private final Bucket bucket;
        private volatile Instant lastAccess;

        private ClientBucket(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccess = Instant.now();
        }

        private Bucket bucket() {
            return bucket;
        }

        private void touch() {
            this.lastAccess = Instant.now();
        }

        private boolean isExpired(Instant now) {
            return Duration.between(lastAccess, now).compareTo(BUCKET_TTL) > 0;
        }
    }
}
