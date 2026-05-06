package pablo.nasc.cash_flow_spring_boot.dto.request.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para criação ou atualização de uma tag")
@Getter
@Setter
public class TagRequest {

    @Schema(
            description = "Nome único da tag dentro do escopo do usuário. Aceita letras, números, #, @ e hífen.",
            example = "#Urgente"
    )
    @NotBlank(message = "O nome da tag é obrigatório")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[\\w#@-]+$", message = "Apenas letras, números, #, @ e hífen são permitidos")
    private String name;

    @Schema(description = "Cor hexadecimal para exibição no frontend", example = "#FF5733")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Informe uma cor hexadecimal válida")
    private String colorHex;
}