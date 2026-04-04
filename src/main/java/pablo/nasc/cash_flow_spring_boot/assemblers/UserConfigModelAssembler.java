package pablo.nasc.cash_flow_spring_boot.assemblers;

import pablo.nasc.cash_flow_spring_boot.controllers.UserConfigController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserController;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler responsável por adicionar links HATEOAS ao UserConfigResponse.
 *
 * Links gerados:
 *   self   → GET   /api/v1/users/me/config
 *   update → PATCH /api/v1/users/me/config
 *   user   → GET   /api/v1/users/me
 */
@Component
public class UserConfigModelAssembler
        extends RepresentationModelAssemblerSupport<UserConfigResponse, UserConfigResponse> {

    public UserConfigModelAssembler() {
        super(UserConfigController.class, UserConfigResponse.class);
    }

    @Override
    public UserConfigResponse toModel(UserConfigResponse response) {
        response.add(
                linkTo(methodOn(UserConfigController.class)
                        .getConfig(null)).withSelfRel(),

                linkTo(methodOn(UserConfigController.class)
                        .updateConfig(null, null)).withRel("update"),

                linkTo(methodOn(UserController.class)
                        .getMe(null)).withRel("user")
        );

        return response;
    }
}
