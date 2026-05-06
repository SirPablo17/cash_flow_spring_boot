package pablo.nasc.cash_flow_spring_boot.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Token de renovação para emissão de um novo par de tokens JWT")
@Getter
@Setter
public class RefreshTokenRequest {

    @Schema(
            description = "Refresh token obtido no login ou registro. Válido por 7 dias.",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    @NotBlank(message = "O refresh token é obrigatório")
    private String refreshToken;
}