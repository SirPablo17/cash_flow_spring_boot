package pablo.nasc.cash_flow_spring_boot.assemblers;

import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.TagController;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler responsável por adicionar links HATEOAS ao TagResponse.
 *
 * Links gerados:
 *   self       → GET    /api/v1/tags/{id}
 *   update     → PUT    /api/v1/tags/{id}
 *   delete     → DELETE /api/v1/tags/{id}
 *   collection → GET    /api/v1/tags
 */
@Component
public class TagModelAssembler
        extends RepresentationModelAssemblerSupport<TagResponse, TagResponse> {

    public TagModelAssembler() {
        super(TagController.class, TagResponse.class);
    }

    @Override
    public TagResponse toModel(TagResponse response) {
        response.add(
                linkTo(methodOn(TagController.class)
                        .getById(response.getId(), null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(TagController.class)
                        .update(response.getId(), null, null)).withRel("atualizar"),

                linkTo(methodOn(TagController.class)
                        .delete(response.getId(), null)).withRel("excluir"),

                linkTo(methodOn(TagController.class)
                        .listAll(null)).withRel("colecao")
        );

        return response;
    }
}
