package pablo.nasc.cash_flow_spring_boot.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração global da documentação OpenAPI / Swagger UI.
 *
 * Acesse em: http://localhost:8080/swagger-ui.html
 *
 * Define:
 *   - Informações gerais da API (título, versão, descrição, contato)
 *   - Esquema de autenticação Bearer JWT
 *     (botão "Authorize" no Swagger para inserir o token)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Dívidas Pessoais")
                        .description("""
                                API RESTful para registro, acompanhamento e organização \
                                de dívidas e compras parceladas.
                                
                                **Autenticação:** utilize o endpoint `/api/v1/auth/login` \
                                para obter o Bearer Token e clique em **Authorize** para \
                                autenticar as requisições.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pablo Nasc")
                                .email("pablo@email.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido no login. Ex: eyJ...")));
    }
}