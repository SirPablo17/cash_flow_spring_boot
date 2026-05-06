package pablo.nasc.cash_flow_spring_boot.dto.response.installment;

import io.swagger.v3.oas.annotations.media.Schema;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Parcela individual gerada a partir de uma dívida")
public class InstallmentResponse extends RepresentationModel<InstallmentResponse> {

    @Schema(description = "Identificador único da parcela", example = "1")
    private Long id;

    @Schema(description = "Número sequencial da parcela dentro da dívida (começa em 1)", example = "3")
    private Integer installmentNumber;

    @Schema(description = "Valor monetário desta parcela (já inclui juros se aplicável)", example = "930.16")
    private BigDecimal amount;

    @Schema(description = "Data de vencimento desta parcela", example = "2025-11-01")
    private LocalDate dueDate;

    @Schema(description = "Data em que o pagamento foi registrado. Nulo enquanto não paga.", example = "2025-10-28", nullable = true)
    private LocalDate paymentDate;

    @Schema(
            description = "Status atual da parcela. PAID e CANCELED são estados terminais.",
            example = "PENDING",
            allowableValues = {"PENDING", "PAID", "OVERDUE", "CANCELED"}
    )
    private PaymentStatus status;

    @Schema(description = "Observações livres sobre a parcela", example = "Pago via Pix", nullable = true)
    private String notes;

    @Schema(description = "ID da dívida à qual esta parcela pertence", example = "5")
    private Long debtId;

    public InstallmentResponse(Long id, Integer installmentNumber, BigDecimal amount,
                               LocalDate dueDate, LocalDate paymentDate, PaymentStatus status,
                               String notes, Long debtId) {
        this.id                 = id;
        this.installmentNumber  = installmentNumber;
        this.amount             = amount;
        this.dueDate            = dueDate;
        this.paymentDate        = paymentDate;
        this.status             = status;
        this.notes              = notes;
        this.debtId             = debtId;
    }

    public Long getId()                     { return id; }
    public Integer getInstallmentNumber()   { return installmentNumber; }
    public BigDecimal getAmount()           { return amount; }
    public LocalDate getDueDate()           { return dueDate; }
    public LocalDate getPaymentDate()       { return paymentDate; }
    public PaymentStatus getStatus()        { return status; }
    public String getNotes()                { return notes; }
    public Long getDebtId()                 { return debtId; }
}