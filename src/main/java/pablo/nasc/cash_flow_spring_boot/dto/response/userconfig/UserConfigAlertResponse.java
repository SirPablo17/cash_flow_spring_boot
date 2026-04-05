package pablo.nasc.cash_flow_spring_boot.dto.response.userconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * DTO de saída para as configurações de alerta do usuário.
 * Endpoint: GET /api/v1/users/me/config/alerts
 */
@Getter
@AllArgsConstructor
public class UserConfigAlertResponse extends RepresentationModel<UserConfigAlertResponse> {

    /** Se os alertas por e-mail estão habilitados */
    private Boolean enableEmailAlerts;

    /** Quantos dias antes do vencimento o alerta é enviado */
    private Integer alertDaysBeforeDue;

    /** Fuso horário para cálculo do momento do alerta */
    private String timezone;
}
