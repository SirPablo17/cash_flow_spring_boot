package pablo.nasc.cash_flow_spring_boot.dto.response.userconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * DTO de saída para a moeda preferida do usuário.
 * Endpoint: GET /api/v1/users/me/config/currency
 */
@Getter
@AllArgsConstructor
public class UserConfigCurrencyResponse extends RepresentationModel<UserConfigCurrencyResponse> {

    /** Código ISO 4217 da moeda preferida (ex: BRL, USD, EUR) */
    private String preferredCurrency;
}
