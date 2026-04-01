package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa cada parcela individual gerada a partir de uma {@link Debt}.
 *
 * É a entidade operacional do dia a dia:
 * o usuário registra pagamentos, cancelamentos e notas aqui.
 *
 * Todas as parcelas são geradas com status = PENDING pelo {@code InstallmentGeneratorService}.
 * O job {@code InstallmentStatusUpdateService} atualiza automaticamente para OVERDUE à meia-noite.
 *
 * Regra de estados terminais: PAID e CANCELED não admitem novas transições → HTTP 422.
 *
 * Tabela: tb_installments
 */
@Entity
@Table(name = "tb_installments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ── Campos gerados automaticamente ───────────────────────────────────────

    /**
     * Número sequencial da parcela dentro da dívida (1, 2, 3...).
     * Gerado pelo InstallmentGeneratorService.
     */
    @NotNull
    @Min(1)
    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    /**
     * Valor monetário desta parcela (já com juros aplicados se houver).
     * Calculado pelo InstallmentGeneratorService via strategy pattern.
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "O valor da parcela deve ser maior que 0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Data de vencimento desta parcela.
     * Calculada como: startDate + (installmentNumber - 1) meses.
     */
    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // ── Campos operacionais (preenchidos pelo usuário) ────────────────────────

    /**
     * Data em que o pagamento foi efetivamente realizado.
     * Nulo enquanto a parcela não estiver PAID.
     * Preenchido automaticamente ao chamar PATCH /installments/{id}/pay.
     */
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /**
     * Status atual da parcela no seu ciclo de vida.
     * Valor inicial: PENDING.
     * Armazenado como String no banco para legibilidade.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Observações livres do usuário (ex: "Pago via Pix", "Parcelado no débito").
     * Campo opcional — atualizável via PATCH /installments/{id}/notes.
     */
    @Size(max = 300)
    @Column(length = 300)
    private String notes;

    // ── Relacionamentos ───────────────────────────────────────────────────────

    /**
     * Dívida-mãe desta parcela.
     * JsonIgnore evita loop Installment → Debt → installments → Installment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    @JsonIgnore
    private Debt debt;

    // ── Métodos utilitários ───────────────────────────────────────────────────

    /**
     * Verifica se a parcela está em estado terminal (PAID ou CANCELED).
     * Usado pelo serviço para lançar BusinessException antes de qualquer mutação.
     */
    public boolean isTerminal() {
        return this.status == PaymentStatus.PAID
                || this.status == PaymentStatus.CANCELED;
    }

    /**
     * Verifica se a parcela está em atraso.
     */
    public boolean isOverdue() {
        return this.status == PaymentStatus.OVERDUE;
    }
}