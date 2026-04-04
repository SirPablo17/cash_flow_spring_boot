package pablo.nasc.cash_flow_spring_boot.dto.response.installment;

import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Links adicionados pelo InstallmentModelAssembler.
 *
 * Links condicionais com base no status:
 *   - pay    → apenas se PENDING ou OVERDUE
 *   - cancel → apenas se PENDING ou OVERDUE
 *   - notes  → sempre disponível
 *   - debt   → sempre disponível (recurso pai)
 */
@Getter
@AllArgsConstructor
public class InstallmentResponse extends RepresentationModel<InstallmentResponse> {

    private Long id;
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String notes;
    private Long debtId;
}