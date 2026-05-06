package pablo.nasc.cash_flow_spring_boot.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para registro de um novo usuário")
@Getter
@Setter
public class RegisterRequest {

    @Schema(description = "Nome completo do usuário", example = "Pablo Nasc", minLength = 2, maxLength = 100)
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100)
    private String name;

    @Schema(description = "E-mail único usado como login", example = "pablo@email.com")
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150)
    private String email;

    @Schema(description = "Senha com no mínimo 8 caracteres", example = "senha123", minLength = 8, maxLength = 255)
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 255)
    private String password;
}