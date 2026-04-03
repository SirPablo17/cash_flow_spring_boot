package pablo.nasc.cash_flow_spring_boot.dto.response.installment;

import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de saída para parcelas individuais.
 * Endpoints: GET /installments | GET /installments/{id}
 *            PATCH /installments/{id}/pay
 *            PATCH /installments/{id}/cancel
 *            PATCH /installments/{id}/notes
 *            GET /debts/{id}/installments
 */
@Getter
@AllArgsConstructor
public class InstallmentResponse {

    private Long id;
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String notes;

    /** Id da dívida-mãe — permite o cliente navegar para GET /debts/{debtId} */
    private Long debtId;
}