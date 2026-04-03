package pablo.nasc.cash_flow_spring_boot.dto.request.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para alteração de senha do usuário autenticado.
 * Endpoint: PATCH /api/v1/users/me/password
 *
 * Exige a senha atual para confirmar a identidade antes de trocar.
 * Retorna HTTP 204 No Content em caso de sucesso.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "A senha atual é obrigatória")
    private String currentPassword;

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 255, message = "A nova senha deve ter entre 8 e 255 caracteres")
    private String newPassword;
}
