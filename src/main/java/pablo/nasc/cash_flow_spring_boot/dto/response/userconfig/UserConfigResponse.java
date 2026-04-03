package pablo.nasc.cash_flow_spring_boot.dto.response.userconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de saída para as configurações do usuário autenticado.
 * Endpoints: GET /users/me/config | PATCH /users/me/config
 */
@Getter
@AllArgsConstructor
public class UserConfigResponse {

    private Long id;
    private String preferredCurrency;
    private Boolean enableEmailAlerts;
    private Integer alertDaysBeforeDue;
    private String timezone;
}