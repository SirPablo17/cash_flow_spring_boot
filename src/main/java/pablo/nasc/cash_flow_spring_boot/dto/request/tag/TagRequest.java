package pablo.nasc.cash_flow_spring_boot.dto.request.tag;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para criação e atualização de tags.
 * Endpoints: POST /api/v1/tags | PUT /api/v1/tags/{id}
 */
@Getter
@Setter
public class TagRequest {

    @NotBlank(message = "O nome da tag é obrigatório")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    @Pattern(
            regexp = "^[\\w#@-]+$",
            message = "O nome só pode conter letras, números, #, @ e hífen"
    )
    private String name;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Informe uma cor hexadecimal válida (ex: #FF5733)"
    )
    private String colorHex;
}
