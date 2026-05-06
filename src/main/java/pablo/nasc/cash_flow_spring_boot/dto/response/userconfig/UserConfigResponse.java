package pablo.nasc.cash_flow_spring_boot.dto.response.userconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

@Schema(description = "Configurações pessoais do usuário autenticado")
public class UserConfigResponse extends RepresentationModel<UserConfigResponse> {

    @Schema(description = "Identificador único das configurações", example = "1")
    private Long id;

    @Schema(description = "Código ISO 4217 da moeda preferida", example = "BRL")
    private String preferredCurrency;

    @Schema(description = "Se os alertas por e-mail de vencimento estão habilitados", example = "true")
    private Boolean enableEmailAlerts;

    @Schema(description = "Dias de antecedência para envio do alerta antes do vencimento", example = "3")
    private Integer alertDaysBeforeDue;

    @Schema(description = "Fuso horário no formato IANA", example = "America/Sao_Paulo")
    private String timezone;

    public UserConfigResponse(Long id, String preferredCurrency, Boolean enableEmailAlerts,
                              Integer alertDaysBeforeDue, String timezone) {
        this.id                 = id;
        this.preferredCurrency  = preferredCurrency;
        this.enableEmailAlerts  = enableEmailAlerts;
        this.alertDaysBeforeDue = alertDaysBeforeDue;
        this.timezone           = timezone;
    }

    public Long getId()                     { return id; }
    public String getPreferredCurrency()    { return preferredCurrency; }
    public Boolean getEnableEmailAlerts()   { return enableEmailAlerts; }
    public Integer getAlertDaysBeforeDue()  { return alertDaysBeforeDue; }
    public String getTimezone()             { return timezone; }
}