package pablo.nasc.cash_flow_spring_boot.infrastructure;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    @Test
    void limitsV1AuthEndpointsAfterTenRequestsPerMinute() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = doFilter("/api/v1/auth/login", "203.0.113.10", calls);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        MockHttpServletResponse blocked = doFilter("/api/v1/auth/login", "203.0.113.10", calls);

        assertThat(calls).hasValue(10);
        assertThat(blocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(blocked.getHeader("X-RateLimit-Limit")).isEqualTo("10");
        assertThat(blocked.getContentAsString()).contains("\"path\":\"/api/v1/auth/login\"");
    }

    @Test
    void limitsV2AuthEndpointsAfterTenRequestsPerMinute() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = doFilter("/api/v2/autenticacao/entrar", "203.0.113.20", calls);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        MockHttpServletResponse blocked = doFilter("/api/v2/autenticacao/entrar", "203.0.113.20", calls);

        assertThat(calls).hasValue(10);
        assertThat(blocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(blocked.getHeader("X-RateLimit-Limit")).isEqualTo("10");
        assertThat(blocked.getContentAsString()).contains("\"path\":\"/api/v2/autenticacao/entrar\"");
    }

    @Test
    void limitsGeneralApiEndpointsAfterSixtyRequestsPerMinute() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse response = doFilter("/api/v1/debts", "203.0.113.30", calls);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        MockHttpServletResponse blocked = doFilter("/api/v1/debts", "203.0.113.30", calls);

        assertThat(calls).hasValue(60);
        assertThat(blocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(blocked.getHeader("X-RateLimit-Limit")).isEqualTo("60");
        assertThat(blocked.getContentAsString()).contains("\"path\":\"/api/v1/debts\"");
    }

    @Test
    void limitsPublicNonApiEndpointsAfterSixtyRequestsPerMinute() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse response = doFilter("/swagger-ui/index.html", "203.0.113.40", calls);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        MockHttpServletResponse blocked = doFilter("/swagger-ui/index.html", "203.0.113.40", calls);

        assertThat(calls).hasValue(60);
        assertThat(blocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(blocked.getHeader("X-RateLimit-Limit")).isEqualTo("60");
        assertThat(blocked.getContentAsString()).contains("\"path\":\"/swagger-ui/index.html\"");
    }

    private MockHttpServletResponse doFilter(String path, String ip, AtomicInteger calls) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRemoteAddr(ip);

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, trackingChain(calls));
        return response;
    }

    private FilterChain trackingChain(AtomicInteger calls) {
        return (request, response) -> calls.incrementAndGet();
    }
}
