package pablo.nasc.cash_flow_spring_boot.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerRegisterRequestCompletesWithCreatedResponse() throws Exception {
        String email = "swagger-" + UUID.randomUUID() + "@email.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, "dev-api-key-change-me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL)
                        .content("""
                                {
                                  "name": "Swagger Test",
                                  "email": "%s",
                                  "password": "senha123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-RateLimit-Limit", "10"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
