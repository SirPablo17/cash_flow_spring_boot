package pablo.nasc.cash_flow_spring_boot.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para atualização do perfil do usuário autenticado")
@Getter
@Setter
public class UserUpdateRequest {

    @Schema(description = "Novo nome completo do usuário", example = "Pablo Nasc Atualizado", minLength = 2, maxLength = 100)
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100)
    private String name;

    @Schema(
            description = "Novo e-mail do usuário. Se alterado, novos tokens JWT serão retornados na resposta.",
            example = "pablo.novo@email.com"
    )
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150)
    private String email;
}