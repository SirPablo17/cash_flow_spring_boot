package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade raiz do sistema.
 * Representa o usuário autenticado dono dos dados financeiros.
 * Implementa soft delete via campo {@code active}.
 *
 * Tabela: tb_users
 */
@Entity
@Table(name = "tb_users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Armazena hash BCrypt — NUNCA deve ser serializado na resposta.
     */
    @NotBlank
    @Size(min = 8, max = 255)
    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @NotNull
    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Relacionamentos ───────────────────────────────────────────────────────

    /**
     * Configurações do usuário criadas automaticamente via cascade.
     * mappedBy indica que a FK está em UserConfig.
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private UserConfig userConfig;

    /**
     * Todas as dívidas do usuário.
     * JsonIgnore evita loop infinito na serialização JSON.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Debt> debts = new ArrayList<>();
}