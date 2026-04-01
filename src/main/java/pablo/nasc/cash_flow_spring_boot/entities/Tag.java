package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Etiquetas livres para organização personalizada de dívidas.
 * Uma tag pode estar em múltiplas dívidas e
 * uma dívida pode ter múltiplas tags (N:M).
 *
 * Esta entidade é o lado INVERSO da relação Many-to-Many.
 * A tabela de junção (tb_debts_tags) é gerenciada pela entidade {@link Debt}.
 *
 * Operação de exclusão: hard delete (registro removido fisicamente do banco).
 *
 * Tabela: tb_tags
 */
@Entity
@Table(name = "tb_tags")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nome único da tag.
     * Permite caracteres alfanuméricos, #, @ e hífen (ex: #Urgente, @CartaoNubank).
     */
    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(
            regexp = "^[\\w#@-]+$",
            message = "O nome da tag só pode conter letras, números, #, @ e hífen"
    )
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Cor hexadecimal para exibição no frontend (ex: #FF5733).
     * Campo opcional.
     */
    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Deve ser uma cor hexadecimal válida (ex: #FF5733)"
    )
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    // ── Relacionamentos ───────────────────────────────────────────────────────

    /**
     * Lado inverso da relação N:M com Debt.
     * NÃO é dono do relacionamento — mappedBy aponta para o campo "tags" em Debt.
     * JsonIgnore evita loop Tag → Debt → Tag na serialização.
     */
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Debt> debts = new ArrayList<>();
}