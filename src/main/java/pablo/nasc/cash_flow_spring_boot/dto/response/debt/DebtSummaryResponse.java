package pablo.nasc.cash_flow_spring_boot.dto.response.debt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Usado em listagens paginadas — não inclui parcelas.
 * Links adicionados pelo DebtModelAssembler.
 */
@Getter
@AllArgsConstructor
public class DebtSummaryResponse extends RepresentationModel<DebtSummaryResponse> {

    private Long id;
    private String title;
    private String creditor;
    private BigDecimal totalAmount;
    private Integer totalInstallments;
    private LocalDate startDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private CategoryResponse category;
    private List<TagResponse> tags;
}