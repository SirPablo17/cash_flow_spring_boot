package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Etiqueta livre — agora privada por usuário.
 * Cada usuário gerencia suas próprias tags.
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
     * Usuário dono desta tag.
     * Nomes de tags agora são únicos por usuário — não mais globalmente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(
            regexp = "^[\\w#@-]+$",
            message = "O nome da tag só pode conter letras, números, #, @ e hífen"
    )
    @Column(nullable = false, length = 50)
    private String name;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Informe uma cor hexadecimal válida (ex: #FF5733)"
    )
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Debt> debts = new ArrayList<>();
}