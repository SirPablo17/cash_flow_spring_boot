package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import pablo.nasc.cash_flow_spring_boot.assemblers.UserConfigModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.userconfig.UserConfigUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigAlertResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigCurrencyResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.userconfig.UserConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Configurações", description = "Preferências pessoais do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users/me/config")
@RequiredArgsConstructor
public class UserConfigController {

    private final UserConfigService userConfigService;
    private final UserRepository userRepository;
    private final UserConfigModelAssembler assembler;

    @Operation(
            summary = "Retornar configurações completas",
            description = "Retorna todas as preferências do usuário: moeda, alertas e fuso horário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<UserConfigResponse> getConfig(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(
                assembler.toModel(userConfigService.getConfig(resolveUserId(principal)))
        );
    }

    @Operation(
            summary = "Retornar configurações de alerta",
            description = "Retorna apenas as configurações de alerta: " +
                    "se os alertas estão ativos, quantos dias de antecedência e fuso horário. " +
                    "Consulta personalizada focada nas preferências de notificação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações de alerta retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/alerts")
    public ResponseEntity<UserConfigAlertResponse> getAlertConfig(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(
                userConfigService.getAlertConfig(resolveUserId(principal))
        );
    }

    @Operation(
            summary = "Retornar moeda preferida",
            description = "Retorna apenas o código ISO 4217 da moeda preferida do usuário (ex: BRL, USD)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Moeda retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/currency")
    public ResponseEntity<UserConfigCurrencyResponse> getCurrency(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(
                userConfigService.getCurrency(resolveUserId(principal))
        );
    }

    @Operation(
            summary = "Atualizar configurações",
            description = "Atualiza as preferências do usuário. " +
                    "preferredCurrency deve ser um código ISO 4217 válido (ex: BRL, USD, EUR). " +
                    "alertDaysBeforeDue deve estar entre 1 e 30 dias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações atualizadas"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping
    public ResponseEntity<UserConfigResponse> updateConfig(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserConfigUpdateRequest request) {

        return ResponseEntity.ok(
                assembler.toModel(userConfigService.updateConfig(resolveUserId(principal), request))
        );
    }

    @Operation(
            summary = "Restaurar configurações padrão",
            description = "Restaura todas as configurações para os valores padrão do sistema: " +
                    "BRL, alertas ativos, 3 dias de antecedência, America/Sao_Paulo. " +
                    "Operação executada via UPDATE em lote no banco (consulta personalizada JPQL)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações restauradas para o padrão"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/reset")
    public ResponseEntity<UserConfigResponse> resetToDefaults(
            @AuthenticationPrincipal UserDetails principal) {

        return ResponseEntity.ok(
                assembler.toModel(userConfigService.resetToDefaults(resolveUserId(principal)))
        );
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}