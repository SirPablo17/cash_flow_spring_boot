package pablo.nasc.cash_flow_spring_boot.dto.response.category;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

@Schema(description = "Categoria de despesa do usuário")
public class CategoryResponse extends RepresentationModel<CategoryResponse> {

    @Schema(description = "Identificador único da categoria", example = "1")
    private Long id;

    @Schema(description = "Nome da categoria", example = "Eletrônicos")
    private String name;

    @Schema(description = "Descrição opcional da categoria", example = "Compras de eletrônicos e tecnologia")
    private String description;

    @Schema(description = "Código de ícone para exibição no frontend", example = "fa-laptop")
    private String iconCode;

    @Schema(description = "Se a categoria está ativa. Categorias inativas não aceitam novas dívidas.", example = "true")
    private Boolean active;

    public CategoryResponse(Long id, String name, String description,
                            String iconCode, Boolean active) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.iconCode    = iconCode;
        this.active      = active;
    }

    public Long getId()             { return id; }
    public String getName()         { return name; }
    public String getDescription()  { return description; }
    public String getIconCode()     { return iconCode; }
    public Boolean getActive()      { return active; }
}