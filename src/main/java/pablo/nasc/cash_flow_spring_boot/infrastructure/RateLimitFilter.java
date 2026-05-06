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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int      AUTH_LIMIT    = 10;
    private static final Duration AUTH_REFILL   = Duration.ofMinutes(1);

    private static final int      GENERAL_LIMIT  = 60;
    private static final Duration GENERAL_REFILL = Duration.ofMinutes(1);

    private final Map<String, Bucket> authBuckets    = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String  ip             = extractClientIp(request);
        boolean isAuthEndpoint = request.getRequestURI().contains("/api/v1/auth");

        Bucket bucket = isAuthEndpoint
                ? authBuckets.computeIfAbsent(ip,    k -> buildBucket(AUTH_LIMIT,    AUTH_REFILL))
                : generalBuckets.computeIfAbsent(ip, k -> buildBucket(GENERAL_LIMIT, GENERAL_REFILL));

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
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}