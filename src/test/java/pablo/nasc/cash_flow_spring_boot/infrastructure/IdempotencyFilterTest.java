package pablo.nasc.cash_flow_spring_boot.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyFilterTest {

    private final IdempotencyFilter filter = new IdempotencyFilter();

    @Test
    void replaysCachedResponseForSameKeyAndBody() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FilterChain chain = (request, response) -> {
            calls.incrementAndGet();
            var httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.CREATED.value());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.addHeader(HttpHeaders.LOCATION, "/api/v1/debts/1");
            httpResponse.getWriter().write("{\"id\":1}");
        };

        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(request("{\"title\":\"A\"}"), firstResponse, chain);

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(request("{\"title\":\"A\"}"), secondResponse, chain);

        assertThat(calls).hasValue(1);
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(secondResponse.getContentAsString()).isEqualTo("{\"id\":1}");
        assertThat(secondResponse.getHeader("Idempotency-Replayed")).isEqualTo("true");
        assertThat(secondResponse.getHeader(HttpHeaders.LOCATION)).isEqualTo("/api/v1/debts/1");
    }

    @Test
    void rejectsSameKeyWithDifferentBody() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FilterChain chain = (request, response) -> {
            calls.incrementAndGet();
            ((HttpServletResponse) response).setStatus(HttpStatus.OK.value());
        };

        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(request("{\"title\":\"A\"}"), firstResponse, chain);

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(request("{\"title\":\"B\"}"), secondResponse, chain);

        assertThat(calls).hasValue(1);
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    private MockHttpServletRequest request(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/debts");
        request.addHeader(IdempotencyFilter.IDEMPOTENCY_KEY_HEADER, "same-key");
        request.addHeader(ApiKeyAuthFilter.API_KEY_HEADER, "secret-key");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }
}
