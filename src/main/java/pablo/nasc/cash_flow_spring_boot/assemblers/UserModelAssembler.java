package pablo.nasc.cash_flow_spring_boot.assemblers;

import pablo.nasc.cash_flow_spring_boot.controllers.UserConfigController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserController;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler responsável por adicionar links HATEOAS ao UserResponse.
 *
 * Links gerados:
 *   self       → GET  /api/v1/users/me
 *   update     → PUT  /api/v1/users/me
 *   password   → PATCH /api/v1/users/me/password
 *   deactivate → DELETE /api/v1/users/me
 *   config     → GET  /api/v1/users/me/config
 */
@Component
public class UserModelAssembler
        extends RepresentationModelAssemblerSupport<UserResponse, UserResponse> {

    public UserModelAssembler() {
        super(UserController.class, UserResponse.class);
    }

    @Override
    public UserResponse toModel(UserResponse response) {
        response.add(
                linkTo(methodOn(UserController.class)
                        .getMe(null)).withSelfRel(),

                linkTo(methodOn(UserController.class)
                        .update(null, null)).withRel("update"),

                linkTo(methodOn(UserController.class)
                        .changePassword(null, null)).withRel("password"),

                linkTo(methodOn(UserController.class)
                        .deactivate(null)).withRel("deactivate"),

                linkTo(methodOn(UserConfigController.class)
                        .getConfig(null)).withRel("config")
        );

        return response;
    }
}