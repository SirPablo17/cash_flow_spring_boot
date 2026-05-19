package pablo.nasc.cash_flow_spring_boot.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String configuredApiKey;

    public ApiKeyAuthFilter(@Value("${app.security.api-key:}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.equals("/api/v1")
                || path.equals("/api/v1/")
                || path.equals("/api/v2")
                || path.equals("/api/v2/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v1/api-docs")
                || path.startsWith("/v2/api-docs")
                || path.equals("/internal/api-docs/swagger-config")
                || path.startsWith("/h2-console")
                || !isVersionedApiPath(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String providedApiKey = request.getHeader(API_KEY_HEADER);

        if (!StringUtils.hasText(configuredApiKey) || !matches(configuredApiKey, providedApiKey)) {
            writeUnauthorized(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean matches(String expected, String provided) {
        if (!StringUtils.hasText(provided)) {
            return false;
        }

        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", "Chave de API ausente ou invalida.",
                "path", request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private boolean isVersionedApiPath(String path) {
        return path.startsWith("/api/v1/") || path.startsWith("/api/v2/");
    }
}
