package pablo.nasc.cash_flow_spring_boot.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;

/**
 * DTO de saída para o resumo financeiro do usuário.
 * Endpoint: GET /api/v1/users/me/summary
 */
@Getter
@AllArgsConstructor
public class UserFinancialSummaryResponse extends RepresentationModel<UserFinancialSummaryResponse> {

    /** Total de dívidas ativas do usuário */
    private Long totalDebts;

    /** Soma dos valores totais de todas as dívidas ativas */
    private BigDecimal totalAmount;

    /** Total de parcelas em todas as dívidas */
    private Long totalInstallments;

    /** Soma dos valores das parcelas ainda PENDING */
    private BigDecimal pendingAmount;
}
