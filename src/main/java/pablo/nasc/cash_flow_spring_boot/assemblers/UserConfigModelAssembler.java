package pablo.nasc.cash_flow_spring_boot.assemblers;

import org.springframework.hateoas.RepresentationModel;
import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserConfigController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserController;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigAlertResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigCurrencyResponse;
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
        return addConfigNavigation(response, true);
    }

    public UserConfigAlertResponse toAlertModel(UserConfigAlertResponse response) {
        response.add(
                linkTo(methodOn(UserConfigController.class)
                        .getAlertConfig(null)).withSelfRel(),

                linkTo(methodOn(UserConfigController.class)
                        .getConfig(null)).withRel("configuracoes")
        );

        return addConfigNavigation(response, false);
    }

    public UserConfigCurrencyResponse toCurrencyModel(UserConfigCurrencyResponse response) {
        response.add(
                linkTo(methodOn(UserConfigController.class)
                        .getCurrency(null)).withSelfRel(),

                linkTo(methodOn(UserConfigController.class)
                        .getConfig(null)).withRel("configuracoes")
        );

        return addConfigNavigation(response, false);
    }

    private <T extends RepresentationModel<T>> T addConfigNavigation(T response, boolean includeSelf) {
        if (includeSelf) {
            response.add(
                    linkTo(methodOn(UserConfigController.class)
                            .getConfig(null)).withSelfRel()
            );
        }

        response.add(
                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(UserConfigController.class)
                        .updateConfig(null, null)).withRel("atualizar"),

                linkTo(methodOn(UserConfigController.class)
                        .getAlertConfig(null)).withRel("alertas"),

                linkTo(methodOn(UserConfigController.class)
                        .getCurrency(null)).withRel("moeda"),

                linkTo(methodOn(UserConfigController.class)
                        .resetToDefaults(null)).withRel("restaurar-padroes"),

                linkTo(methodOn(UserController.class)
                        .getMe(null)).withRel("usuario")
        );

        return response;
    }
}
