package pablo.nasc.cash_flow_spring_boot.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para renovação do access token via refresh token.
 * Endpoint: POST /api/v1/auth/refresh
 */
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "O refresh token é obrigatório")
    private String refreshToken;
}
