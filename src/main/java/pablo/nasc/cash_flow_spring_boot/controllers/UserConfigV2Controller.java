package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.dto.request.userconfig.UserConfigUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigAlertResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigCurrencyResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.userconfig.UserConfigService;

@Tag(name = "Configuracoes v2", description = "Preferencias pessoais em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/usuarios/eu/configuracoes")
@RequiredArgsConstructor
public class UserConfigV2Controller {

    private final UserConfigService userConfigService;
    private final UserRepository userRepository;

    @Operation(summary = "Retornar configuracoes completas")
    @GetMapping
    public ResponseEntity<UserConfigResponse> getConfig(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(userConfigService.getConfig(resolveUserId(principal)));
    }

    @Operation(summary = "Retornar configuracoes de alerta")
    @GetMapping("/alertas")
    public ResponseEntity<UserConfigAlertResponse> getAlertConfig(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(userConfigService.getAlertConfig(resolveUserId(principal)));
    }

    @Operation(summary = "Retornar moeda preferida")
    @GetMapping("/moeda")
    public ResponseEntity<UserConfigCurrencyResponse> getCurrency(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(userConfigService.getCurrency(resolveUserId(principal)));
    }

    @Operation(summary = "Atualizar configuracoes")
    @PatchMapping
    public ResponseEntity<UserConfigResponse> updateConfig(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserConfigUpdateRequest request) {

        return ResponseEntity.ok(userConfigService.updateConfig(resolveUserId(principal), request));
    }

    @Operation(summary = "Restaurar configuracoes padrao")
    @PostMapping("/restaurar")
    public ResponseEntity<UserConfigResponse> resetToDefaults(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(userConfigService.resetToDefaults(resolveUserId(principal)));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
