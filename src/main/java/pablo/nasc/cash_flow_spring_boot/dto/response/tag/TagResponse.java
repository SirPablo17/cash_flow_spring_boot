package pablo.nasc.cash_flow_spring_boot.dto.response.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

@Schema(description = "Etiqueta do usuário para organização de dívidas")
public class TagResponse extends RepresentationModel<TagResponse> {

    @Schema(description = "Identificador único da tag", example = "1")
    private Long id;

    @Schema(description = "Nome da tag", example = "#Urgente")
    private String name;

    @Schema(description = "Cor hexadecimal para exibição no frontend", example = "#FF5733")
    private String colorHex;

    public TagResponse(Long id, String name, String colorHex) {
        this.id       = id;
        this.name     = name;
        this.colorHex = colorHex;
    }

    public Long getId()         { return id; }
    public String getName()     { return name; }
    public String getColorHex() { return colorHex; }
}