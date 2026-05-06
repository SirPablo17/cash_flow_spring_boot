package pablo.nasc.cash_flow_spring_boot.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Par de tokens JWT retornado após autenticação ou renovação")
@Getter
@AllArgsConstructor
public class AuthResponse {

    @Schema(description = "Token de acesso de curta duração (24h). Use no header Authorization: Bearer {token}", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Token de renovação de longa duração (7 dias). Use em POST /auth/refresh para obter novos tokens.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Esquema de autenticação — sempre Bearer", example = "Bearer")
    private String tokenType = "Bearer";

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}