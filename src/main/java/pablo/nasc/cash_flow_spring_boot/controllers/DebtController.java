package pablo.nasc.cash_flow_spring_boot.controllers;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller do recurso principal — Debt.
 * Todos os endpoints exigem Bearer Token válido.
 * Base URL: /api/v1/debts
 *
 * Aplicação do ISP: injeta DebtReadService e DebtWriteService separadamente.
 */
@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtReadService debtReadService;
    private final DebtWriteService debtWriteService;
    private final InstallmentServiceImpl installmentService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/debts
     * Lista dívidas do usuário com filtros opcionais e paginação.
     * Query params: active, categoryId, tagId, page, size, sort
     * Retorna 200 OK.
     */
    @GetMapping
    public ResponseEntity<Page<DebtSummaryResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            Pageable pageable) {

        return ResponseEntity.ok(
                debtReadService.listDebts(resolveUserId(principal), active, categoryId, tagId, pageable)
        );
    }

    /**
     * GET /api/v1/debts/{id}
     * Retorna uma dívida completa com parcelas e tags.
     * Valida ownership — retorna 403 se a dívida pertencer a outro usuário.
     * Retorna 200 OK, 403 Forbidden ou 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(debtReadService.getDebt(id, resolveUserId(principal)));
    }

    /**
     * POST /api/v1/debts
     * Cria uma dívida e gera automaticamente todas as parcelas.
     * Retorna 201 Created, 400 Bad Request ou 422 Unprocessable Entity.
     */
    @PostMapping
    public ResponseEntity<DebtResponse> create(
            @Valid @RequestBody DebtCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(debtWriteService.createDebt(request, resolveUserId(principal)));
    }

    /**
     * PUT /api/v1/debts/{id}
     * Atualiza campos informativos (título, descrição, credor).
     * NÃO regenera parcelas.
     * Retorna 200 OK, 403 Forbidden ou 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DebtUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(debtWriteService.updateDebt(id, request, resolveUserId(principal)));
    }

    /**
     * DELETE /api/v1/debts/{id}
     * Soft delete + cancela parcelas PENDING e OVERDUE.
     * Parcelas PAID são preservadas.
     * Retorna 204 No Content, 403 Forbidden ou 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        debtWriteService.deleteDebt(id, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/debts/{id}/installments
     * Lista todas as parcelas de uma dívida ordenadas por número.
     * Valida ownership antes de listar.
     * Retorna 200 OK, 403 Forbidden ou 404 Not Found.
     */
    @GetMapping("/{id}/installments")
    public ResponseEntity<List<InstallmentResponse>> listInstallments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        // Valida que a dívida existe e pertence ao usuário
        debtReadService.getDebt(id, resolveUserId(principal));

        return ResponseEntity.ok(installmentService.listByDebt(id));
    }

    /**
     * POST /api/v1/debts/{id}/tags/{tagId}
     * Associa uma tag à dívida.
     * Retorna 200 OK, 404 Not Found ou 409 Conflict se já associada.
     */
    @PostMapping("/{id}/tags/{tagId}")
    public ResponseEntity<DebtResponse> addTag(
            @PathVariable Long id,
            @PathVariable Long tagId,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(debtWriteService.addTag(id, tagId, resolveUserId(principal)));
    }

    /**
     * DELETE /api/v1/debts/{id}/tags/{tagId}
     * Remove a associação de uma tag com a dívida.
     * Retorna 204 No Content ou 404 Not Found.
     */
    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Void> removeTag(
            @PathVariable Long id,
            @PathVariable Long tagId,
            @AuthenticationPrincipal UserDetails principal) {

        debtWriteService.removeTag(id, tagId, resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}