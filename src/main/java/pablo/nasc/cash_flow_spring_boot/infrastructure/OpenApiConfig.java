package pablo.nasc.cash_flow_spring_boot.infrastructure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.util.StringUtils;

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
    public GlobalOpenApiCustomizer rateLimitResponseCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation ->
                            operation.getResponses().addApiResponse("429", tooManyRequestsResponse())
                    )
            );
        };
    }

    @Bean
    public ServerBaseUrlCustomizer serverBaseUrlCustomizer() {
        return (serverBaseUrl, request) -> {
            String forwardedBaseUrl = forwardedBaseUrl(request);
            if (forwardedBaseUrl != null) {
                return forwardedBaseUrl;
            }

            String host = firstHeaderValue(request, "Host");
            if (StringUtils.hasText(host)) {
                return request.getURI().getScheme() + "://" + host;
            }

            return serverBaseUrl;
        };
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

    private ApiResponse tooManyRequestsResponse() {
        return new ApiResponse()
                .description("Too Many Requests - limite de requisicoes por minuto atingido. "
                        + "Verifique o header Retry-After para saber quantos segundos aguardar.");
    }

    private String forwardedBaseUrl(HttpRequest request) {
        String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
        if (!StringUtils.hasText(forwardedHost)) {
            return null;
        }

        String forwardedProto = firstHeaderValue(request, "X-Forwarded-Proto");
        String scheme = StringUtils.hasText(forwardedProto)
                ? forwardedProto
                : request.getURI().getScheme();

        String forwardedPort = firstHeaderValue(request, "X-Forwarded-Port");
        String host = appendPortIfNeeded(forwardedHost, scheme, forwardedPort);

        return scheme + "://" + host;
    }

    private String appendPortIfNeeded(String host, String scheme, String port) {
        if (!StringUtils.hasText(port) || host.contains(":")) {
            return host;
        }

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && "80".equals(port))
                || ("https".equalsIgnoreCase(scheme) && "443".equals(port));

        return defaultPort ? host : host + ":" + port;
    }

    private String firstHeaderValue(HttpRequest request, String headerName) {
        String value = request.getHeaders().getFirst(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.split(",")[0].trim();
    }
}
