package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade central do sistema.
 * Representa uma dívida consolidada (ex: "Compra do iPhone 16 Pro no cartão").
 *
 * Ao ser persistida, dispara a geração automática de todas as {@link Installment}
 * via {@code InstallmentGeneratorService}.
 *
 * Implementa soft delete — exclusão lógica via campo {@code active}.
 * Ao desativar uma Debt, parcelas PENDING e OVERDUE são canceladas automaticamente;
 * parcelas PAID são preservadas para manter o histórico.
 *
 * Tabela: tb_debts
 * Tabela de junção com Tag: tb_debts_tags (debt_id FK + tag_id FK)
 */
@Entity
@Table(name = "tb_debts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ── Campos de dados ───────────────────────────────────────────────────────

    /**
     * Título descritivo da dívida (ex: "Compra do iPhone 16 Pro").
     */
    @NotBlank
    @Size(min = 3, max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    /**
     * Detalhamento opcional (ex: "Comprado na loja Apple com juros de 1,99% a.m.").
     */
    @Size(max = 500)
    @Column(length = 500)
    private String description;

    /**
     * Valor total da dívida antes do parcelamento.
     * Mínimo: R$ 0,01 | Máximo: 13 dígitos inteiros, 2 decimais.
     */
    @NotNull
    @DecimalMin(value = "0.01", message = "O valor total deve ser maior que 0.01")
    @Digits(integer = 13, fraction = 2)
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Número total de parcelas. Mínimo: 1 (à vista). Máximo: 360 (30 anos).
     */
    @NotNull
    @Min(value = 1, message = "A dívida deve ter pelo menos 1 parcela")
    @Max(value = 360, message = "O número de parcelas não pode exceder 360")
    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    /**
     * Data de vencimento da 1ª parcela.
     * Não pode ser no passado — validado por @FutureOrPresent.
     */
    @NotNull
    @FutureOrPresent(message = "A data de início não pode ser no passado")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Taxa de juros mensal em formato decimal (ex: 0.0199 = 1,99% a.m.).
     * Nulo ou zero → parcelas calculadas sem juros (divisão simples).
     * Com valor → cálculo pela Tabela Price (Sistema Francês).
     * Range: 0.0 a 1.0 (0% a 100% a.m.)
     */
    @DecimalMin(value = "0.0", message = "A taxa de juros não pode ser negativa")
    @DecimalMax(value = "1.0", message = "A taxa de juros não pode exceder 100% ao mês")
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;

    /**
     * Nome do credor (ex: Nubank, Apple Store, Banco do Brasil).
     * Campo opcional.
     */
    @Size(max = 100)
    @Column(length = 100)
    private String creditor;

    /**
     * Soft delete — false significa que a dívida foi cancelada
     * e não aparece em listagens padrão.
     */
    @NotNull
    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Relacionamentos ───────────────────────────────────────────────────────

    /**
     * Usuário dono desta dívida.
     * A FK user_id é extraída do JWT pelo controller.
     * JsonIgnore evita loop Debt → User → debts → Debt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * Categoria desta dívida.
     * Obrigatório — categoria inativa gera HTTP 422.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Parcelas geradas automaticamente ao criar a Debt.
     * cascade=ALL → persiste/remove junto com a Debt.
     * orphanRemoval=true → remove parcelas órfãs do banco.
     */
    @OneToMany(
            mappedBy = "debt",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Installment> installments = new ArrayList<>();

    /**
     * Tags associadas a esta dívida (N:M).
     * Esta entidade É DONA do relacionamento — gerencia a tabela de junção.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_debts_tags",
            joinColumns        = @JoinColumn(name = "debt_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
}