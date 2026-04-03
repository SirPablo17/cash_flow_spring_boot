package pablo.nasc.cash_flow_spring_boot.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de saída para os endpoints de autenticação.
 * Endpoints: POST /auth/register | POST /auth/login | POST /auth/refresh
 *
 * Retorna o par de tokens JWT para o cliente armazenar
 * e usar nas requisições subsequentes via header:
 *   Authorization: Bearer {accessToken}
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    /** Token de acesso de curta duração (ex: 24h). Usado no header Authorization. */
    private String accessToken;

    /** Token de renovação de longa duração (ex: 7 dias). Usado em POST /auth/refresh. */
    private String refreshToken;

    /** Sempre "Bearer" — informa ao cliente o esquema de autenticação. */
    private String tokenType = "Bearer";

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
    }
}
