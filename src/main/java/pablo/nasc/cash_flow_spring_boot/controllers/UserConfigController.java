package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.dto.request.userconfig.UserConfigUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.userconfig.UserConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de configurações do usuário autenticado.
 * Todos os endpoints exigem Bearer Token válido.
 * Base URL: /api/v1/users/me/config
 */
@RestController
@RequestMapping("/api/v1/users/me/config")
@RequiredArgsConstructor
public class UserConfigController {

    private final UserConfigService userConfigService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/users/me/config
     * Retorna as configurações do usuário autenticado.
     * Retorna 200 OK.
     */
    @GetMapping
    public ResponseEntity<UserConfigResponse> getConfig(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userConfigService.getConfig(resolveUserId(principal)));
    }

    /**
     * PATCH /api/v1/users/me/config
     * Atualiza parcialmente as configurações do usuário autenticado.
     * Retorna 200 OK.
     */
    @PatchMapping
    public ResponseEntity<UserConfigResponse> updateConfig(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserConfigUpdateRequest request) {
        return ResponseEntity.ok(
                userConfigService.updateConfig(resolveUserId(principal), request)
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
