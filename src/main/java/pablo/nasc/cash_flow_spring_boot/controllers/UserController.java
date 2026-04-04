package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.assemblers.UserModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserWithTokenResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserModelAssembler assembler;

    /**
     * GET /api/v1/users/me
     * Retorna dados do usuário autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal UserDetails principal) {
        UserResponse response = userService.getMe(resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    /**
     * PUT /api/v1/users/me
     * Atualiza nome e/ou e-mail do usuário autenticado.
     *
     * Retorna UserWithTokenResponse com novos tokens JWT.
     * O cliente DEVE substituir os tokens armazenados pelos retornados aqui,
     * pois o e-mail pode ter mudado e o token antigo não será mais válido.
     */
    @PutMapping("/me")
    public ResponseEntity<UserWithTokenResponse> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserUpdateRequest request) {
        UserWithTokenResponse response = userService.update(resolveUserId(principal), request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/users/me/password
     * Altera a senha do usuário autenticado.
     * Retorna 204 No Content.
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(resolveUserId(principal), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/v1/users/me
     * Desativa a conta do usuário autenticado (soft delete).
     * Retorna 204 No Content.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal UserDetails principal) {
        userService.deactivate(resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}