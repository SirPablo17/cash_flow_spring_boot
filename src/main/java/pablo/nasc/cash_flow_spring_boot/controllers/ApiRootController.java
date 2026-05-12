package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Descoberta", description = "Ponto inicial da API com links para os principais recursos")
@RestController
@RequestMapping("/api/v1")
public class ApiRootController {

    @Operation(
            summary = "Descobrir recursos da API",
            description = "Retorna links HATEOAS para autenticação, usuário, configurações, dívidas, parcelas, categorias e tags."
    )
    @GetMapping
    public ResponseEntity<RepresentationModel<?>> index() {
        RepresentationModel<?> root = new RepresentationModel<>();

        root.add(
                linkTo(methodOn(ApiRootController.class).index()).withSelfRel(),
                linkTo(methodOn(AuthController.class).register(null)).withRel("cadastro"),
                linkTo(methodOn(AuthController.class).login(null)).withRel("entrar"),
                linkTo(methodOn(AuthController.class).refresh(null)).withRel("renovar-token"),
                linkTo(methodOn(UserController.class).getMe(null)).withRel("usuario"),
                linkTo(methodOn(UserController.class).getSummary(null)).withRel("resumo-financeiro"),
                linkTo(methodOn(UserConfigController.class).getConfig(null)).withRel("configuracoes"),
                linkTo(methodOn(UserConfigController.class).getAlertConfig(null)).withRel("alertas"),
                linkTo(methodOn(UserConfigController.class).getCurrency(null)).withRel("moeda"),
                linkTo(methodOn(DebtController.class).list(null, null, null, null, null, null)).withRel("dividas"),
                linkTo(methodOn(InstallmentController.class).list(null, null, null, null, null, null)).withRel("parcelas"),
                linkTo(methodOn(CategoryController.class).listActive(null, null, null)).withRel("categorias"),
                linkTo(methodOn(TagController.class).listAll(null)).withRel("tags")
        );

        return ResponseEntity.ok(root);
    }
}
