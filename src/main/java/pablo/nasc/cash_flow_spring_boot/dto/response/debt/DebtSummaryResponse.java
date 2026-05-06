package pablo.nasc.cash_flow_spring_boot.dto.response.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Resumo de uma dívida — usado na listagem paginada. Não inclui parcelas.")
public class DebtSummaryResponse extends RepresentationModel<DebtSummaryResponse> {

    @Schema(description = "Identificador único da dívida", example = "1")
    private Long id;

    @Schema(description = "Título descritivo da dívida", example = "Compra do iPhone 16 Pro")
    private String title;

    @Schema(description = "Nome do credor ou instituição financeira", example = "Apple Store")
    private String creditor;

    @Schema(description = "Valor total da dívida antes do parcelamento", example = "9999.00")
    private BigDecimal totalAmount;

    @Schema(description = "Número total de parcelas geradas", example = "12")
    private Integer totalInstallments;

    @Schema(description = "Data de vencimento da primeira parcela", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "Se a dívida está ativa. false = cancelada via soft delete.", example = "true")
    private Boolean active;

    @Schema(description = "Data e hora de criação da dívida no sistema")
    private LocalDateTime createdAt;

    @Schema(description = "Categoria à qual esta dívida pertence")
    private CategoryResponse category;

    @Schema(description = "Tags associadas a esta dívida")
    private List<TagResponse> tags;

    public DebtSummaryResponse(Long id, String title, String creditor,
                               BigDecimal totalAmount, Integer totalInstallments,
                               LocalDate startDate, Boolean active, LocalDateTime createdAt,
                               CategoryResponse category, List<TagResponse> tags) {
        this.id                = id;
        this.title             = title;
        this.creditor          = creditor;
        this.totalAmount       = totalAmount;
        this.totalInstallments = totalInstallments;
        this.startDate         = startDate;
        this.active            = active;
        this.createdAt         = createdAt;
        this.category          = category;
        this.tags              = tags;
    }

    public Long getId()                     { return id; }
    public String getTitle()                { return title; }
    public String getCreditor()             { return creditor; }
    public BigDecimal getTotalAmount()      { return totalAmount; }
    public Integer getTotalInstallments()   { return totalInstallments; }
    public LocalDate getStartDate()         { return startDate; }
    public Boolean getActive()              { return active; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public CategoryResponse getCategory()   { return category; }
    public List<TagResponse> getTags()      { return tags; }
}