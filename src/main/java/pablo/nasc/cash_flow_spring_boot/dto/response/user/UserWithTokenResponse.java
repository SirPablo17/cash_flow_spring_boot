package pablo.nasc.cash_flow_spring_boot.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Schema(
        description = "Dados atualizados do usuário + novo par de tokens JWT. " +
                "Retornado no PUT /users/me — substitua os tokens armazenados pelos retornados aqui."
)
@Getter
@Setter
@AllArgsConstructor
public class UserWithTokenResponse extends RepresentationModel<UserWithTokenResponse> {

    @Schema(description = "Identificador único do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome atualizado do usuário", example = "Pablo Nasc Atualizado")
    private String name;

    @Schema(description = "E-mail atualizado do usuário — usado como subject do novo token", example = "pablo.novo@email.com")
    private String email;

    @Schema(description = "Status da conta", example = "true")
    private Boolean active;

    @Schema(description = "Data de criação da conta")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização")
    private LocalDateTime updatedAt;

    @Schema(description = "Novo access token gerado com o e-mail atualizado. Válido por 24h.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Novo refresh token gerado com o e-mail atualizado. Válido por 7 dias.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Esquema de autenticação", example = "Bearer")
    private String tokenType = "Bearer";

    public UserWithTokenResponse(Long id, String name, String email, Boolean active,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 String accessToken, String refreshToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}