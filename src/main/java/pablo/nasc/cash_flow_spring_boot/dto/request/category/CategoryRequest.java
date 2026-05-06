package pablo.nasc.cash_flow_spring_boot.dto.request.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para criação ou atualização de uma categoria de despesa")
@Getter
@Setter
public class CategoryRequest {

    @Schema(description = "Nome único da categoria dentro do escopo do usuário", example = "Eletrônicos", minLength = 2, maxLength = 80)
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 2, max = 80)
    private String name;

    @Schema(description = "Descrição opcional da categoria", example = "Compras de eletrônicos e tecnologia")
    @Size(max = 255)
    private String description;

    @Schema(description = "Código de ícone para exibição no frontend", example = "fa-laptop")
    @Size(max = 30)
    private String iconCode;
}