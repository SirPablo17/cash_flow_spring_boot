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

@Tag(name = "Descoberta v2", description = "Ponto inicial da API v2 com endpoints em portugues")
@RestController
@RequestMapping("/api/v2")
public class ApiRootV2Controller {

    @Operation(summary = "Descobrir recursos da API v2")
    @GetMapping
    public ResponseEntity<RepresentationModel<?>> index() {
        RepresentationModel<?> root = new RepresentationModel<>();

        root.add(
                linkTo(methodOn(ApiRootV2Controller.class).index()).withSelfRel(),
                linkTo(methodOn(AuthV2Controller.class).register(null)).withRel("cadastro"),
                linkTo(methodOn(AuthV2Controller.class).login(null)).withRel("entrar"),
                linkTo(methodOn(AuthV2Controller.class).refresh(null)).withRel("renovar-token"),
                linkTo(methodOn(UserV2Controller.class).getMe(null)).withRel("usuario"),
                linkTo(methodOn(UserV2Controller.class).getSummary(null)).withRel("resumo-financeiro"),
                linkTo(methodOn(UserConfigV2Controller.class).getConfig(null)).withRel("configuracoes"),
                linkTo(methodOn(UserConfigV2Controller.class).getAlertConfig(null)).withRel("alertas"),
                linkTo(methodOn(UserConfigV2Controller.class).getCurrency(null)).withRel("moeda"),
                linkTo(methodOn(DebtV2Controller.class).list(null, null, null, null, null)).withRel("dividas"),
                linkTo(methodOn(InstallmentV2Controller.class).list(null, null, null, null, null)).withRel("parcelas"),
                linkTo(methodOn(CategoryV2Controller.class).listActive(null, null)).withRel("categorias"),
                linkTo(methodOn(TagV2Controller.class).listAll(null)).withRel("etiquetas"),
                linkTo(methodOn(ExportV2Controller.class).exportCashFlowExcel(null)).withRel("exportar-fluxo-caixa")
        );

        return ResponseEntity.ok(root);
    }
}
