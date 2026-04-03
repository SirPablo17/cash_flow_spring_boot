package pablo.nasc.cash_flow_spring_boot.dto.request.auth;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para autenticação do usuário.
 * Endpoint: POST /api/v1/auth/login
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String password;
}