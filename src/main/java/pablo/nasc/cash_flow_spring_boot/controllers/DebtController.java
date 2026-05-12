package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import pablo.nasc.cash_flow_spring_boot.assemblers.DebtModelAssembler;
import pablo.nasc.cash_flow_spring_boot.assemblers.InstallmentModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtCreateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtSummaryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.debt.DebtReadService;
import pablo.nasc.cash_flow_spring_boot.services.debt.DebtWriteService;
import pablo.nasc.cash_flow_spring_boot.services.installment.InstallmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Dívidas", description = "Recurso principal — gerenciamento de dívidas e parcelas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtReadService debtReadService;
    private final DebtWriteService debtWriteService;
    private final InstallmentServiceImpl installmentService;
    private final UserRepository userRepository;
    private final DebtModelAssembler debtAssembler;
    private final InstallmentModelAssembler installmentAssembler;


    @Operation(
            summary = "Listar dívidas do usuário",
            description = "Retorna as dívidas do usuário autenticado com filtros opcionais e paginação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<DebtSummaryResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<DebtSummaryResponse> pagedAssembler) {

        Page<DebtSummaryResponse> page = debtReadService
                .listDebts(resolveUserId(principal), active, categoryId, tagId, pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(page, debtAssembler::toSummaryModel));
    }

    @Operation(
            summary = "Buscar dívida por ID",
            description = "Retorna os detalhes completos de uma dívida incluindo parcelas e tags."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dívida encontrada"),
            @ApiResponse(responseCode = "403", description = "Dívida pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getById(
            @Parameter(description = "ID da dívida") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        DebtResponse response = debtReadService.getDebt(id, resolveUserId(principal));
        return ResponseEntity.ok(debtAssembler.toModel(response));
    }

    @Operation(
            summary = "Criar dívida",
            description = "Cria uma nova dívida e gera automaticamente todas as parcelas. " +
                    "Se informado interestRate, o cálculo usa a Tabela Price (Sistema Francês). " +
                    "Sem juros, o valor é dividido igualmente entre as parcelas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dívida criada com parcelas geradas automaticamente"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Categoria inativa ou inexistente",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<DebtResponse> create(
            @Valid @RequestBody DebtCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        DebtResponse response = debtWriteService.createDebt(request, resolveUserId(principal));
        URI location = linkTo(methodOn(DebtController.class).getById(response.getId(), null)).toUri();

        return ResponseEntity.created(location).body(debtAssembler.toModel(response));
    }

    @Operation(
            summary = "Atualizar dívida",
            description = "Atualiza apenas campos informativos (título, descrição, credor). " +
                    "Campos financeiros não são atualizáveis para preservar as parcelas geradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dívida atualizada"),
            @ApiResponse(responseCode = "403", description = "Dívida pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> update(
            @Parameter(description = "ID da dívida") @PathVariable Long id,
            @Valid @RequestBody DebtUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        DebtResponse response = debtWriteService.updateDebt(id, request, resolveUserId(principal));
        return ResponseEntity.ok(debtAssembler.toModel(response));
    }

    @Operation(
            summary = "Cancelar dívida",
            description = "Realiza soft delete da dívida. " +
                    "Parcelas PENDING e OVERDUE são canceladas automaticamente. " +
                    "Parcelas PAID são preservadas para manter o histórico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dívida cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Dívida pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da dívida") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        debtWriteService.deleteDebt(id, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar parcelas de uma dívida",
            description = "Retorna todas as parcelas de uma dívida ordenadas por número."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcelas retornadas"),
            @ApiResponse(responseCode = "403", description = "Dívida pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}/installments")
    public ResponseEntity<CollectionModel<InstallmentResponse>> listInstallments(
            @Parameter(description = "ID da dívida") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        debtReadService.getDebt(id, resolveUserId(principal));

        List<InstallmentResponse> installments = installmentService.listByDebt(id)
                .stream()
                .map(installmentAssembler::toModel)
                .toList();

        CollectionModel<InstallmentResponse> collection = CollectionModel.of(
                installments,
                linkTo(methodOn(DebtController.class).listInstallments(id, null)).withSelfRel(),
                linkTo(methodOn(DebtController.class).getById(id, null)).withRel("divida")
        );

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Associar tag à dívida")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag associada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Dívida ou tag não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Tag já associada a esta dívida",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/{id}/tags/{tagId}")
    public ResponseEntity<DebtResponse> addTag(
            @Parameter(description = "ID da dívida") @PathVariable Long id,
            @Parameter(description = "ID da tag") @PathVariable Long tagId,
            @AuthenticationPrincipal UserDetails principal) {

        DebtResponse response = debtWriteService.addTag(id, tagId, resolveUserId(principal));
        return ResponseEntity.ok(debtAssembler.toModel(response));
    }

    @Operation(summary = "Remover tag de uma dívida")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Dívida ou tag não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Void> removeTag(
            @Parameter(description = "ID da dívida") @PathVariable Long id,
            @Parameter(description = "ID da tag") @PathVariable Long tagId,
            @AuthenticationPrincipal UserDetails principal) {

        debtWriteService.removeTag(id, tagId, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
