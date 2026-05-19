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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.dto.request.installment.InstallmentNotesRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.common.PaginaResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.installment.InstallmentServiceImpl;

import java.time.LocalDate;

@Tag(name = "Parcelas v2", description = "Pagamento e controle de parcelas em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/parcelas")
@RequiredArgsConstructor
public class InstallmentV2Controller {

    private final InstallmentServiceImpl installmentService;
    private final UserRepository userRepository;

    @Operation(summary = "Listar parcelas do usuario")
    @GetMapping
    public ResponseEntity<PaginaResponse<InstallmentResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(name = "situacao", required = false) PaymentStatus status,
            @RequestParam(name = "dataVencimentoInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateStart,
            @RequestParam(name = "dataVencimentoFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateEnd,
            @ParameterObject Pageable pageable) {

        Page<InstallmentResponse> page = installmentService
                .listByUser(resolveUserId(principal), status, dueDateStart, dueDateEnd, pageable);

        return ResponseEntity.ok(PaginaResponse.de(page));
    }

    @Operation(summary = "Buscar parcela por ID")
    @GetMapping("/{id}")
    public ResponseEntity<InstallmentResponse> getById(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(installmentService.getById(id, resolveUserId(principal)));
    }

    @Operation(summary = "Registrar pagamento de parcela")
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<InstallmentResponse> pay(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(installmentService.pay(id, resolveUserId(principal)));
    }

    @Operation(summary = "Cancelar parcela")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<InstallmentResponse> cancel(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(installmentService.cancel(id, resolveUserId(principal)));
    }

    @Operation(summary = "Atualizar observacoes da parcela")
    @PatchMapping("/{id}/observacoes")
    public ResponseEntity<InstallmentResponse> updateNotes(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @Valid @RequestBody InstallmentNotesRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(installmentService.updateNotes(
                id, resolveUserId(principal), request
        ));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
