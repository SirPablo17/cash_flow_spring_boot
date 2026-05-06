package pablo.nasc.cash_flow_spring_boot.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Schema(description = "Dados do usuário autenticado")
public class UserResponse extends RepresentationModel<UserResponse> {

    @Schema(description = "Identificador único do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "Pablo Nasc")
    private String name;

    @Schema(description = "E-mail de login do usuário", example = "pablo@email.com")
    private String email;

    @Schema(description = "Se a conta está ativa. false = conta desativada (soft delete)", example = "true")
    private Boolean active;

    @Schema(description = "Data e hora de criação da conta")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização do perfil")
    private LocalDateTime updatedAt;

    public UserResponse(Long id, String name, String email, Boolean active,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id        = id;
        this.name      = name;
        this.email     = email;
        this.active    = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public Boolean getActive()       { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}