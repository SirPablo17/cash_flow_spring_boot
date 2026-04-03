package pablo.nasc.cash_flow_spring_boot.dto.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de saída para categorias.
 * Endpoints: GET /categories | GET /categories/{id} | POST /categories | PUT /categories/{id}
 */
@Getter
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String iconCode;
    private Boolean active;
}
