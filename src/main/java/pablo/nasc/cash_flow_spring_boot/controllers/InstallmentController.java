package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import pablo.nasc.cash_flow_spring_boot.assemblers.InstallmentModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.installment.InstallmentNotesRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.installment.InstallmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Parcelas", description = "Recurso operacional — pagamento e controle de parcelas")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/installments")
@RequiredArgsConstructor
public class InstallmentController {

    private final InstallmentServiceImpl installmentService;
    private final UserRepository userRepository;
    private final InstallmentModelAssembler assembler;


    @Operation(
            summary = "Listar parcelas do usuário",
            description = "Retorna todas as parcelas do usuário autenticado com filtros opcionais. " +
                    "Filtros: status (PENDING, PAID, OVERDUE, CANCELED), " +
                    "dueDateStart e dueDateEnd (formato: yyyy-MM-dd)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcelas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<InstallmentResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateStart,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateEnd,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<InstallmentResponse> pagedAssembler) {

        Page<InstallmentResponse> page = installmentService
                .listByUser(resolveUserId(principal), status, dueDateStart, dueDateEnd, pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar parcela por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcela encontrada"),
            @ApiResponse(responseCode = "403", description = "Parcela pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Parcela não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<InstallmentResponse> getById(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.getById(id, resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @Operation(
            summary = "Registrar pagamento de parcela",
            description = "Marca a parcela como PAID e registra a data de pagamento como hoje. " +
                    "Parcelas em estado terminal (PAID ou CANCELED) retornam 422."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Parcela pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Parcela não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Parcela já está em estado terminal",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}/pay")
    public ResponseEntity<InstallmentResponse> pay(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.pay(id, resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @Operation(
            summary = "Cancelar parcela",
            description = "Marca a parcela como CANCELED. " +
                    "Parcelas em estado terminal (PAID ou CANCELED) retornam 422."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parcela cancelada"),
            @ApiResponse(responseCode = "403", description = "Parcela pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Parcela não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Parcela já está em estado terminal",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InstallmentResponse> cancel(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.cancel(id, resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @Operation(
            summary = "Atualizar observações da parcela",
            description = "Atualiza o campo de observações livres da parcela. " +
                    "Disponível em qualquer status — inclusive em estados terminais."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Observações atualizadas"),
            @ApiResponse(responseCode = "400", description = "Observação excede 300 caracteres",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Parcela pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Parcela não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}/notes")
    public ResponseEntity<InstallmentResponse> updateNotes(
            @Parameter(description = "ID da parcela") @PathVariable Long id,
            @Valid @RequestBody InstallmentNotesRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.updateNotes(
                id, resolveUserId(principal), request
        );
        return ResponseEntity.ok(assembler.toModel(response));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
