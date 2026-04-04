package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.assemblers.InstallmentModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.installment.InstallmentNotesRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.installment.InstallmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/installments")
@RequiredArgsConstructor
public class InstallmentController {

    private final InstallmentServiceImpl installmentService;
    private final UserRepository userRepository;
    private final InstallmentModelAssembler assembler;

    @GetMapping
    public ResponseEntity<Page<InstallmentResponse>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateStart,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateEnd,
            Pageable pageable) {

        Page<InstallmentResponse> response = installmentService
                .listByUser(resolveUserId(principal), status, dueDateStart, dueDateEnd, pageable)
                .map(assembler::toModel);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstallmentResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.getById(id, resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InstallmentResponse> pay(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.pay(id, resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InstallmentResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        InstallmentResponse response = installmentService.cancel(id, resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<InstallmentResponse> updateNotes(
            @PathVariable Long id,
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