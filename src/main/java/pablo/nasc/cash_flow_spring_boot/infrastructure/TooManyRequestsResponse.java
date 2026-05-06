package pablo.nasc.cash_flow_spring_boot.infrastructure;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

/**
 * Anotação reutilizável para documentar o HTTP 429 no Swagger.
 *
 * Em vez de repetir @ApiResponse(responseCode = "429", ...) em todos os controllers,
 * basta anotar o método com @TooManyRequestsResponse.
 *
 * Uso:
 *   @GetMapping
 *   @TooManyRequestsResponse
 *   public ResponseEntity<...> list(...) { }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse(
        responseCode = "429",
        description = "Too Many Requests — limite de requisições por minuto atingido. " +
                "Verifique o header 'Retry-After' para saber quantos segundos aguardar.",
        content = @Content(schema = @Schema(hidden = true))
)
public @interface TooManyRequestsResponse {
}