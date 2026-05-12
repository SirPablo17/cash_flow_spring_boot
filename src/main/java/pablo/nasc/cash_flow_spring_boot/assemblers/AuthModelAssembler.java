package pablo.nasc.cash_flow_spring_boot.assemblers;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.AuthController;
import pablo.nasc.cash_flow_spring_boot.controllers.CategoryController;
import pablo.nasc.cash_flow_spring_boot.controllers.DebtController;
import pablo.nasc.cash_flow_spring_boot.controllers.InstallmentController;
import pablo.nasc.cash_flow_spring_boot.controllers.TagController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserConfigController;
import pablo.nasc.cash_flow_spring_boot.controllers.UserController;
import pablo.nasc.cash_flow_spring_boot.dto.response.auth.AuthResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthModelAssembler
        extends RepresentationModelAssemblerSupport<AuthResponse, AuthResponse> {

    public AuthModelAssembler() {
        super(AuthController.class, AuthResponse.class);
    }

    @Override
    public AuthResponse toModel(AuthResponse response) {
        response.add(
                linkTo(methodOn(ApiRootController.class).index()).withRel("inicio"),
                linkTo(methodOn(AuthController.class).refresh(null)).withRel("renovar-token"),
                linkTo(methodOn(UserController.class).getMe(null)).withRel("usuario"),
                linkTo(methodOn(UserConfigController.class).getConfig(null)).withRel("configuracoes"),
                linkTo(methodOn(DebtController.class).list(null, null, null, null, null, null)).withRel("dividas"),
                linkTo(methodOn(InstallmentController.class).list(null, null, null, null, null, null)).withRel("parcelas"),
                linkTo(methodOn(CategoryController.class).listActive(null, null, null)).withRel("categorias"),
                linkTo(methodOn(TagController.class).listAll(null)).withRel("tags")
        );

        return response;
    }
}
