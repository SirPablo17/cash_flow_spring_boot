package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de operações do usuário autenticado.
 * Todos os endpoints exigem Bearer Token válido.
 * Base URL: /api/v1/users
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/users/me
     * Retorna os dados do usuário autenticado.
     * Retorna 200 OK.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getMe(resolveUserId(principal)));
    }

    /**
     * PUT /api/v1/users/me
     * Atualiza nome e/ou e-mail do usuário autenticado.
     * Retorna 200 OK ou 409 Conflict se o e-mail já estiver em uso.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(resolveUserId(principal), request));
    }

    /**
     * PATCH /api/v1/users/me/password
     * Altera a senha do usuário autenticado.
     * Exige a senha atual para confirmar identidade.
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