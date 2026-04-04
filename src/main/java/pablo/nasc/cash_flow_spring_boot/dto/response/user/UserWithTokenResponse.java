package pablo.nasc.cash_flow_spring_boot.dto.response.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

/**
 * DTO de saída para o endpoint PUT /users/me.
 *
 * Retorna os dados atualizados do usuário + novo par de tokens JWT.
 * Necessário porque o e-mail é o subject do JWT — se o e-mail mudar,
 * o token antigo não será mais válido e o cliente ficaria desautenticado.
 *
 * O cliente deve sempre substituir os tokens armazenados pelos retornados aqui.
 */
@Getter
@Setter
public class UserWithTokenResponse extends RepresentationModel<UserWithTokenResponse> {

    private Long id;
    private String name;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Novo access token gerado com o e-mail atualizado */
    private String accessToken;

    /** Novo refresh token gerado com o e-mail atualizado */
    private String refreshToken;

    private String tokenType = "Bearer";

    public UserWithTokenResponse(Long id, String name, String email, Boolean active,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 String accessToken, String refreshToken) {
        this.id           = id;
        this.name         = name;
        this.email        = email;
        this.active       = active;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
    }
}