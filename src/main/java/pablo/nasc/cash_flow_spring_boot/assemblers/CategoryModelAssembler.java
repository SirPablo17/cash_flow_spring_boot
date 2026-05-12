package pablo.nasc.cash_flow_spring_boot.assemblers;

import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.CategoryController;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler responsável por adicionar links HATEOAS ao CategoryResponse.
 *
 * Links gerados:
 *   self       → GET    /api/v1/categories/{id}
 *   update     → PUT    /api/v1/categories/{id}
 *   deactivate → DELETE /api/v1/categories/{id}
 *   collection → GET    /api/v1/categories
 */
@Component
public class CategoryModelAssembler
        extends RepresentationModelAssemblerSupport<CategoryResponse, CategoryResponse> {

    public CategoryModelAssembler() {
        super(CategoryController.class, CategoryResponse.class);
    }

    @Override
    public CategoryResponse toModel(CategoryResponse response) {
        response.add(
                linkTo(methodOn(CategoryController.class)
                        .getById(response.getId(), null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(CategoryController.class)
                        .update(response.getId(), null, null)).withRel("atualizar"),

                linkTo(methodOn(CategoryController.class)
                        .listActive(null, null, null)).withRel("colecao")
        );

        if (Boolean.TRUE.equals(response.getActive())) {
            response.add(
                    linkTo(methodOn(CategoryController.class)
                            .delete(response.getId(), null)).withRel("desativar")
            );
        }

        return response;
    }
}
