package pablo.nasc.cash_flow_spring_boot.dto.response.debt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Retornado no detalhe completo de uma dívida.
 * Links adicionados pelo DebtModelAssembler.
 */
@Getter
@AllArgsConstructor
public class DebtResponse extends RepresentationModel<DebtResponse> {

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
    private CategoryResponse category;
    private List<TagResponse> tags;
    private List<InstallmentResponse> installments;
}
