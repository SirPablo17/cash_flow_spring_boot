package pablo.nasc.cash_flow_spring_boot.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Credenciais para autenticação do usuário")
@Getter
@Setter
public class LoginRequest {

    @Schema(description = "E-mail cadastrado no sistema", example = "pablo@email.com")
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    private String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    @NotBlank(message = "A senha é obrigatória")
    private String password;
}