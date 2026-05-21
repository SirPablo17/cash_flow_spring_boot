package pablo.nasc.cash_flow_spring_boot.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiVersionDocsTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> HTTP_METHODS = Set.of(
            "get",
            "post",
            "put",
            "patch",
            "delete",
            "options",
            "head",
            "trace"
    );

    @Test
    void v1DocsDoNotExposeV2Endpoints() throws Exception {
        mockMvc.perform(get("/v1/api-docs").header("Host", "localhost:8080"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"url\":\"http://localhost:8080\"")))
                .andExpect(content().string(not(containsString("https://cash-flow-spring-boot.onrender.com"))))
                .andExpect(content().string(containsString("/api/v1/auth/login")))
                .andExpect(content().string(not(containsString("/api/v2/exportacoes/fluxo-caixa/excel"))));
    }

    @Test
    void serverBaseUrlCustomizerUsesRenderForwardedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/api-docs");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "cash-flow-spring-boot.onrender.com");

        String serverBaseUrl = new OpenApiConfig()
                .serverBaseUrlCustomizer()
                .customize("http://localhost:8080", new ServletServerHttpRequest(request));

        assertThat(serverBaseUrl).isEqualTo("https://cash-flow-spring-boot.onrender.com");
    }

    @Test
    void v2DocsExposeOnlyV2Endpoints() throws Exception {
        mockMvc.perform(get("/v2/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/v2/exportacoes/fluxo-caixa/excel")))
                .andExpect(content().string(containsString("/api/v2/dividas")))
                .andExpect(content().string(containsString("/api/v2/parcelas")))
                .andExpect(content().string(containsString("/api/v2/categorias")))
                .andExpect(content().string(containsString("/api/v2/etiquetas")))
                .andExpect(content().string(not(containsString("/api/v1/auth/login"))));
    }

    @Test
    void v1DocsDocumentTooManyRequestsForEveryEndpoint() throws Exception {
        assertEveryEndpointDocumentsTooManyRequests("/v1/api-docs");
    }

    @Test
    void v2DocsDocumentTooManyRequestsForEveryEndpoint() throws Exception {
        assertEveryEndpointDocumentsTooManyRequests("/v2/api-docs");
    }

    private void assertEveryEndpointDocumentsTooManyRequests(String docsPath) throws Exception {
        MvcResult result = mockMvc.perform(get(docsPath))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsString()).path("paths");
        assertThat(paths.size()).isPositive();

        paths.fields().forEachRemaining(pathEntry ->
                pathEntry.getValue().fields().forEachRemaining(operationEntry -> {
                    if (!HTTP_METHODS.contains(operationEntry.getKey())) {
                        return;
                    }

                    assertThat(operationEntry.getValue().path("responses").has("429"))
                            .as("%s %s documents 429", operationEntry.getKey().toUpperCase(), pathEntry.getKey())
                            .isTrue();
                })
        );
    }
}
