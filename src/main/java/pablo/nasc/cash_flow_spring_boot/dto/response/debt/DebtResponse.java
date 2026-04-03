package pablo.nasc.cash_flow_spring_boot.dto.response.debt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de saída completo para detalhes de uma dívida.
 * Endpoint: GET /api/v1/debts/{id}
 *
 * Inclui todos os campos e a lista completa de parcelas,
 * diferente do {@link DebtSummaryResponse} usado nas listagens.
 */
@Getter
@AllArgsConstructor
public class DebtResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal totalAmount;
    private Integer totalInstallments;
    private LocalDate startDate;
    private BigDecimal interestRate;
    private String creditor;
    private Boolean active;
    private LocalDateTime createdAt;

    /** Categoria completa da dívida */
    private CategoryResponse category;

    /** Tags associadas à dívida */
    private List<TagResponse> tags;

    /** Lista completa de parcelas geradas */
    private List<InstallmentResponse> installments;
}
