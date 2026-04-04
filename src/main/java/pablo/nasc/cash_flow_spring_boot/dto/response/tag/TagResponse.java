package pablo.nasc.cash_flow_spring_boot.dto.response.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Links adicionados pelo TagModelAssembler.
 */
@Getter
@AllArgsConstructor
public class TagResponse extends RepresentationModel<TagResponse> {

    private Long id;
    private String name;
    private String colorHex;
}