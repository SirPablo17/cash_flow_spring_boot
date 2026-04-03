package pablo.nasc.cash_flow_spring_boot.dto.request.category;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para criação e atualização de categorias.
 * Endpoints: POST /api/v1/categories | PUT /api/v1/categories/{id}
 */
@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 2, max = 80, message = "O nome deve ter entre 2 e 80 caracteres")
    private String name;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String description;

    @Size(max = 30, message = "O código do ícone deve ter no máximo 30 caracteres")
    private String iconCode;
}