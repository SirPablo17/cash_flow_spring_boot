package pablo.nasc.cash_flow_spring_boot.dto.request.userconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para atualização das configurações pessoais do usuário")
@Getter
@Setter
public class UserConfigUpdateRequest {

    @Schema(
            description = "Código ISO 4217 da moeda preferida",
            example = "BRL",
            allowableValues = {"BRL", "USD", "EUR", "GBP", "JPY"}
    )
    @NotBlank(message = "A moeda preferida é obrigatória")
    @Size(min = 3, max = 3)
    @Pattern(regexp = "[A-Z]{3}", message = "Deve conter exatamente 3 letras maiúsculas")
    private String preferredCurrency;

    @Schema(description = "Se true, o sistema envia e-mails de lembrete antes do vencimento das parcelas", example = "true")
    @NotNull(message = "Informe se deseja receber alertas por e-mail")
    private Boolean enableEmailAlerts;

    @Schema(description = "Dias de antecedência para envio do alerta de vencimento (entre 1 e 30)", example = "3", minimum = "1", maximum = "30")
    @NotNull(message = "A antecedência do alerta é obrigatória")
    @Min(1) @Max(30)
    private Integer alertDaysBeforeDue;

    @Schema(description = "Fuso horário no formato IANA para cálculo dos alertas", example = "America/Sao_Paulo")
    @NotBlank(message = "O fuso horário é obrigatório")
    @Size(max = 50)
    private String timezone;
}