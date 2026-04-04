package pablo.nasc.cash_flow_spring_boot.dto.response.userconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Links adicionados pelo UserConfigModelAssembler.
 */
@Getter
@AllArgsConstructor
public class UserConfigResponse extends RepresentationModel<UserConfigResponse> {

    private Long id;
    private String preferredCurrency;
    private Boolean enableEmailAlerts;
    private Integer alertDaysBeforeDue;
    private String timezone;
}