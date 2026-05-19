package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.export.CashFlowExcelExportService;

@Tag(name = "Exportacoes v2", description = "Endpoints de exportacao da API v2")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/exportacoes")
@RequiredArgsConstructor
public class ExportV2Controller {

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final CashFlowExcelExportService excelExportService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Exportar fluxo de caixa em Excel",
            description = "Gera um arquivo .xlsx com as abas Dividas e Parcelas do usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo Excel gerado com sucesso",
                    content = @Content(mediaType = XLSX_MEDIA_TYPE)),
            @ApiResponse(responseCode = "401", description = "Chave de API ou token ausente/invalido",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping(value = "/fluxo-caixa/excel", produces = XLSX_MEDIA_TYPE)
    public ResponseEntity<byte[]> exportCashFlowExcel(
            @AuthenticationPrincipal UserDetails principal) {

        byte[] content = excelExportService.exportUserCashFlow(resolveUserId(principal));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fluxo-caixa-v2.xlsx\"")
                .header("X-API-Version", "v2")
                .body(content);
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
