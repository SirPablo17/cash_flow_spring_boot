package pablo.nasc.cash_flow_spring_boot.assemblers;

import org.springframework.hateoas.RepresentationModel;
import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserConfigController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserController;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserFinancialSummaryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserWithTokenResponse;
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
        return addUserNavigation(response);
    }

    public UserWithTokenResponse toTokenModel(UserWithTokenResponse response) {
        return addUserNavigation(response);
    }

    public UserFinancialSummaryResponse toSummaryModel(UserFinancialSummaryResponse response) {
        response.add(
                linkTo(methodOn(UserController.class)
                        .getSummary(null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(UserController.class)
                        .getMe(null)).withRel("usuario"),

                linkTo(methodOn(UserConfigController.class)
                        .getConfig(null)).withRel("configuracoes")
        );

        return response;
    }

    private <T extends RepresentationModel<T>> T addUserNavigation(T response) {
        response.add(
                linkTo(methodOn(UserController.class)
                        .getMe(null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(UserController.class)
                        .update(null, null)).withRel("atualizar"),

                linkTo(methodOn(UserController.class)
                        .changePassword(null, null)).withRel("alterar-senha"),

                linkTo(methodOn(UserController.class)
                        .deactivate(null)).withRel("desativar"),

                linkTo(methodOn(UserConfigController.class)
                        .getConfig(null)).withRel("configuracoes"),

                linkTo(methodOn(UserController.class)
                        .getSummary(null)).withRel("resumo-financeiro")
        );

        return response;
    }
}
