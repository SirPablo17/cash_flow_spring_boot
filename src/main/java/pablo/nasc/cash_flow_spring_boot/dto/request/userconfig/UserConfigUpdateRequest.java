package pablo.nasc.cash_flow_spring_boot.dto.request.userconfig;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para atualização parcial das configurações do usuário.
 * Endpoint: PATCH /api/v1/users/me/config
 *
 * Todos os campos são obrigatórios no PATCH pois substituem
 * as configurações atuais por completo.
 */
@Getter
@Setter
public class UserConfigUpdateRequest {

    @NotBlank(message = "A moeda preferida é obrigatória")
    @Size(min = 3, max = 3, message = "Informe um código ISO 4217 válido (ex: BRL, USD)")
    @Pattern(
            regexp = "[A-Z]{3}",
            message = "O código da moeda deve conter exatamente 3 letras maiúsculas (ex: BRL)"
    )
    private String preferredCurrency;

    @NotNull(message = "Informe se deseja receber alertas por e-mail")
    private Boolean enableEmailAlerts;

    @NotNull(message = "A antecedência do alerta é obrigatória")
    @Min(value = 1,  message = "O alerta deve ser com no mínimo 1 dia de antecedência")
    @Max(value = 30, message = "O alerta deve ser com no máximo 30 dias de antecedência")
    private Integer alertDaysBeforeDue;

    @NotBlank(message = "O fuso horário é obrigatório")
    @Size(max = 50, message = "O fuso horário deve ter no máximo 50 caracteres")
    private String timezone;
}