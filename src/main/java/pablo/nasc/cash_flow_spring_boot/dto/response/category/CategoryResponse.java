package pablo.nasc.cash_flow_spring_boot.dto.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Links adicionados pelo CategoryModelAssembler.
 */
@Getter
@AllArgsConstructor
public class CategoryResponse extends RepresentationModel<CategoryResponse> {

    private Long id;
    private String name;
    private String description;
    private String iconCode;
    private Boolean active;
}