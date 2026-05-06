package pablo.nasc.cash_flow_spring_boot.dto.response.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detalhes completos de uma dívida, incluindo parcelas e tags associadas")
public class DebtResponse extends RepresentationModel<DebtResponse> {

    @Schema(description = "Identificador único da dívida", example = "1")
    private Long id;

    @Schema(description = "Título descritivo da dívida", example = "Compra do iPhone 16 Pro")
    private String title;

    @Schema(description = "Detalhamento da dívida", example = "Comprado na Apple Store com juros de 1,99% a.m.")
    private String description;

    @Schema(description = "Valor total da dívida antes do parcelamento", example = "9999.00")
    private BigDecimal totalAmount;

    @Schema(description = "Número total de parcelas geradas automaticamente", example = "12")
    private Integer totalInstallments;

    @Schema(description = "Data de vencimento da primeira parcela", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "Taxa de juros mensal em decimal. Nulo = sem juros.", example = "0.0199", nullable = true)
    private BigDecimal interestRate;

    @Schema(description = "Nome do credor ou instituição financeira", example = "Apple Store")
    private String creditor;

    @Schema(description = "Se a dívida está ativa. false = cancelada via soft delete.", example = "true")
    private Boolean active;

    @Schema(description = "Data e hora de criação da dívida no sistema")
    private LocalDateTime createdAt;

    @Schema(description = "Categoria à qual esta dívida pertence")
    private CategoryResponse category;

    @Schema(description = "Tags associadas a esta dívida")
    private List<TagResponse> tags;

    @Schema(description = "Lista completa de parcelas geradas")
    private List<InstallmentResponse> installments;

    public DebtResponse(Long id, String title, String description, BigDecimal totalAmount,
                        Integer totalInstallments, LocalDate startDate, BigDecimal interestRate,
                        String creditor, Boolean active, LocalDateTime createdAt,
                        CategoryResponse category, List<TagResponse> tags,
                        List<InstallmentResponse> installments) {
        this.id                = id;
        this.title             = title;
        this.description       = description;
        this.totalAmount       = totalAmount;
        this.totalInstallments = totalInstallments;
        this.startDate         = startDate;
        this.interestRate      = interestRate;
        this.creditor          = creditor;
        this.active            = active;
        this.createdAt         = createdAt;
        this.category          = category;
        this.tags              = tags;
        this.installments      = installments;
    }

    public Long getId()                             { return id; }
    public String getTitle()                        { return title; }
    public String getDescription()                  { return description; }
    public BigDecimal getTotalAmount()              { return totalAmount; }
    public Integer getTotalInstallments()           { return totalInstallments; }
    public LocalDate getStartDate()                 { return startDate; }
    public BigDecimal getInterestRate()             { return interestRate; }
    public String getCreditor()                     { return creditor; }
    public Boolean getActive()                      { return active; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public CategoryResponse getCategory()           { return category; }
    public List<TagResponse> getTags()              { return tags; }
    public List<InstallmentResponse> getInstallments() { return installments; }
}