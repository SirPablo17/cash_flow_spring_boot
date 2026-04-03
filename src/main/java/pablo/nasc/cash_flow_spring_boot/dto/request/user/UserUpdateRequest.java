package pablo.nasc.cash_flow_spring_boot.dto.request.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para atualização dos dados do usuário autenticado.
 * Endpoint: PUT /api/v1/users/me
 *
 * Permite atualizar nome e/ou e-mail.
 * Senha é alterada por endpoint separado (PATCH /users/me/password).
 */
@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email;
}
