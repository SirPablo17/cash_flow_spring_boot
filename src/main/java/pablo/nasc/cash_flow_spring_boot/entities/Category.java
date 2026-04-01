package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa dívidas em categorias de despesa.
 * Uma categoria pode conter múltiplas dívidas,
 * mas cada dívida pertence a apenas uma categoria (1:N).
 *
 * Regra: categorias inativas (active = false) não aceitam novas dívidas → HTTP 422.
 * Implementa soft delete via campo {@code active}.
 *
 * Tabela: tb_categories
 */
@Entity
@Table(name = "tb_categories")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nome único da categoria (ex: Moradia, Saúde, Transporte).
     * Constraint UNIQUE no banco garante sem duplicatas.
     */
    @NotBlank
    @Size(min = 2, max = 80)
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    /**
     * Descrição opcional da categoria.
     */
    @Size(max = 255)
    @Column(length = 255)
    private String description;

    /**
     * Código de ícone para uso no frontend (ex: fa-home, home_icon).
     */
    @Size(max = 30)
    @Column(name = "icon_code", length = 30)
    private String iconCode;

    /**
     * Soft delete — categorias inativas não aparecem em listagens padrão
     * e não aceitam novas dívidas.
     */
    @NotNull
    @Column(nullable = false)
    private Boolean active = true;

    // ── Relacionamentos ───────────────────────────────────────────────────────

    /**
     * Dívidas pertencentes a esta categoria.
     * Lado "um" da relação — FK está em Debt.
     * JsonIgnore evita loop Category → Debt → Category na serialização.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Debt> debts = new ArrayList<>();
}