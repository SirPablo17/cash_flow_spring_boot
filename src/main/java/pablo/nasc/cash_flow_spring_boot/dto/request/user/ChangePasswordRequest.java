package pablo.nasc.cash_flow_spring_boot.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para alteração de senha do usuário autenticado")
@Getter
@Setter
public class ChangePasswordRequest {

    @Schema(description = "Senha atual do usuário para confirmação de identidade", example = "senha123")
    @NotBlank(message = "A senha atual é obrigatória")
    private String currentPassword;

    @Schema(description = "Nova senha desejada com no mínimo 8 caracteres", example = "novaSenha456", minLength = 8, maxLength = 255)
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 255)
    private String newPassword;
}