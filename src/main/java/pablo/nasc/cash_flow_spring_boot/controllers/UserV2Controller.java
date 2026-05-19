package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserFinancialSummaryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserWithTokenResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.user.UserService;

@Tag(name = "Usuarios v2", description = "Gerenciamento do usuario autenticado em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/usuarios")
@RequiredArgsConstructor
public class UserV2Controller {

    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "Retornar dados do usuario autenticado")
    @GetMapping("/eu")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getMe(resolveUserId(principal)));
    }

    @Operation(summary = "Resumo financeiro do usuario")
    @GetMapping("/eu/resumo")
    public ResponseEntity<UserFinancialSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(userService.getFinancialSummary(resolveUserId(principal)));
    }

    @Operation(summary = "Atualizar dados do usuario")
    @PutMapping("/eu")
    public ResponseEntity<UserWithTokenResponse> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(userService.update(resolveUserId(principal), request));
    }

    @Operation(summary = "Alterar senha")
    @PatchMapping("/eu/senha")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(resolveUserId(principal), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desativar conta")
    @DeleteMapping("/eu")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal UserDetails principal) {
        userService.deactivate(resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
