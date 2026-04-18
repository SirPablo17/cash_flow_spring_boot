package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Categoria de despesa — agora privada por usuário.
 * Cada usuário gerencia suas próprias categorias.
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
     * Usuário dono desta categoria.
     * A constraint UNIQUE de nome agora é por usuário — não mais global.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank
    @Size(min = 2, max = 80)
    @Column(nullable = false, length = 80)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Size(max = 30)
    @Column(name = "icon_code", length = 30)
    private String iconCode;

    @NotNull
    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Debt> debts = new ArrayList<>();
}