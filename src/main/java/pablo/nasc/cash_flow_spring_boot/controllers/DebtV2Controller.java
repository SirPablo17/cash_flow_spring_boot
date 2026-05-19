package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtCreateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.common.PaginaResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtSummaryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.debt.DebtReadService;
import pablo.nasc.cash_flow_spring_boot.services.debt.DebtWriteService;
import pablo.nasc.cash_flow_spring_boot.services.installment.InstallmentServiceImpl;

import java.net.URI;
import java.util.List;

@Tag(name = "Dividas v2", description = "Gerenciamento de dividas e parcelas em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/dividas")
@RequiredArgsConstructor
public class DebtV2Controller {

    private final DebtReadService debtReadService;
    private final DebtWriteService debtWriteService;
    private final InstallmentServiceImpl installmentService;
    private final UserRepository userRepository;

    @Operation(summary = "Listar dividas do usuario")
    @GetMapping
    public ResponseEntity<PaginaResponse<DebtSummaryResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Boolean ativa,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long etiquetaId,
            @ParameterObject Pageable pageable) {

        Page<DebtSummaryResponse> page = debtReadService
                .listDebts(resolveUserId(principal), ativa, categoriaId, etiquetaId, pageable);

        return ResponseEntity.ok(PaginaResponse.de(page));
    }

    @Operation(summary = "Buscar divida por ID")
    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getById(
            @Parameter(description = "ID da divida") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(debtReadService.getDebt(id, resolveUserId(principal)));
    }

    @Operation(summary = "Criar divida")
    @PostMapping
    public ResponseEntity<DebtResponse> create(
            @Valid @RequestBody DebtCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        DebtResponse response = debtWriteService.createDebt(request, resolveUserId(principal));
        return ResponseEntity
                .created(URI.create("/api/v2/dividas/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Atualizar divida")
    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> update(
            @Parameter(description = "ID da divida") @PathVariable Long id,
            @Valid @RequestBody DebtUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(debtWriteService.updateDebt(id, request, resolveUserId(principal)));
    }

    @Operation(summary = "Cancelar divida")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da divida") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        debtWriteService.deleteDebt(id, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar parcelas de uma divida")
    @GetMapping("/{id}/parcelas")
    public ResponseEntity<List<InstallmentResponse>> listInstallments(
            @Parameter(description = "ID da divida") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        debtReadService.getDebt(id, resolveUserId(principal));
        return ResponseEntity.ok(installmentService.listByDebt(id));
    }

    @Operation(summary = "Associar etiqueta a divida")
    @PostMapping("/{id}/etiquetas/{etiquetaId}")
    public ResponseEntity<DebtResponse> addTag(
            @Parameter(description = "ID da divida") @PathVariable Long id,
            @Parameter(description = "ID da etiqueta") @PathVariable Long etiquetaId,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(debtWriteService.addTag(id, etiquetaId, resolveUserId(principal)));
    }

    @Operation(summary = "Remover etiqueta de uma divida")
    @DeleteMapping("/{id}/etiquetas/{etiquetaId}")
    public ResponseEntity<Void> removeTag(
            @Parameter(description = "ID da divida") @PathVariable Long id,
            @Parameter(description = "ID da etiqueta") @PathVariable Long etiquetaId,
            @AuthenticationPrincipal UserDetails principal) {

        debtWriteService.removeTag(id, etiquetaId, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
