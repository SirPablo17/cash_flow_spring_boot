package pablo.nasc.cash_flow_spring_boot.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1", "/api/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
                .group("v2")
                .pathsToMatch("/api/v2/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        final String apiKeySchemeName = "apiKeyAuth";
        final String bearerSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Dividas Pessoais")
                        .description("""
                                API RESTful para registro, acompanhamento e organizacao \
                                de dividas e compras parceladas.

                                **Chave de API:** envie o header `X-API-Key` em todas as \
                                rotas versionadas da API.

                                **Usuario autenticado:** nas rotas privadas, tambem envie \
                                `Authorization: Bearer {token}` com o token obtido no login.

                                **Idempotencia:** para operacoes mutaveis, envie \
                                `Idempotency-Key` para permitir retentativas seguras.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SirPablo17")
                                .email("pablo.rosario2019@gmail.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(apiKeySchemeName)
                        .addList(bearerSchemeName))
                .components(new Components()
                        .addSecuritySchemes(apiKeySchemeName, new SecurityScheme()
                                .name(ApiKeyAuthFilter.API_KEY_HEADER)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Informe a chave de API no header X-API-Key."))
                        .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                                .name(bearerSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido no login. Ex: eyJ...")));
    }
}
