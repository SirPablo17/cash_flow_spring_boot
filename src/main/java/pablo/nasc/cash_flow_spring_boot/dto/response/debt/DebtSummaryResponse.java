package pablo.nasc.cash_flow_spring_boot.dto.response.debt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de saída resumido para listagem de dívidas.
 * Endpoint: GET /api/v1/debts (listagem paginada)
 *
 * Não inclui a lista completa de parcelas para evitar respostas pesadas.
 * Para ver as parcelas, o cliente usa GET /debts/{id}/installments.
 */
@Getter
@AllArgsConstructor
public class DebtSummaryResponse {

    private Long id;
    private String title;
    private String creditor;
    private BigDecimal totalAmount;
    private Integer totalInstallments;
    private LocalDate startDate;
    private Boolean active;
    private LocalDateTime createdAt;

    /** Categoria resumida — apenas id e nome para evitar payload desnecessário */
    private CategoryResponse category;

    /** Tags associadas */
    private List<TagResponse> tags;
}