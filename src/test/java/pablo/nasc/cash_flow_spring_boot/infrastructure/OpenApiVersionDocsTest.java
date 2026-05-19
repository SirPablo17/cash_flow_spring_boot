package pablo.nasc.cash_flow_spring_boot.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void v1DocsDoNotExposeV2Endpoints() throws Exception {
        mockMvc.perform(get("/v1/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/v1/auth/login")))
                .andExpect(content().string(not(containsString("/api/v2/exportacoes/fluxo-caixa/excel"))));
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
}
