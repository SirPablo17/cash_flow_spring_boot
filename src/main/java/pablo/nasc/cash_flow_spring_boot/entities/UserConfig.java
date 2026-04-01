package pablo.nasc.cash_flow_spring_boot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Configurações pessoais do usuário.
 * Criada automaticamente no momento do registro via cascade a partir de {@link User}.
 * O campo user_id possui constraint UNIQUE para garantir cardinalidade 1:1.
 *
 * Valores padrão ao criar um User:
 *   preferredCurrency  = "BRL"
 *   enableEmailAlerts  = true
 *   alertDaysBeforeDue = 3
 *   timezone           = "America/Sao_Paulo"
 *
 * Tabela: tb_user_configs
 */
@Entity
@Table(name = "tb_user_configs")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * FK com constraint UNIQUE — garante que 1 User tem exatamente 1 UserConfig.
     * JsonIgnore evita loop User → UserConfig → User na serialização.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    /**
     * Código ISO 4217 da moeda preferida (ex: BRL, USD, EUR).
     * Padrão: "BRL"
     */
    @NotBlank
    @Size(min = 3, max = 3)
    @Pattern(regexp = "[A-Z]{3}", message = "Deve ser um código ISO 4217 válido (ex: BRL, USD)")
    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency = "BRL";

    /**
     * Se true, o sistema envia e-mails de lembrete de parcelas próximas ao vencimento.
     * Padrão: true
     */
    @NotNull
    @Column(name = "enable_email_alerts", nullable = false)
    private Boolean enableEmailAlerts = true;

    /**
     * Quantos dias antes do vencimento o alerta deve ser enviado.
     * Padrão: 3 dias
     */
    @NotNull
    @Min(1)
    @Max(30)
    @Column(name = "alert_days_before_due", nullable = false)
    private Integer alertDaysBeforeDue = 3;

    /**
     * Fuso horário do usuário no formato IANA (ex: America/Sao_Paulo).
     * Padrão: "America/Sao_Paulo"
     */
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String timezone = "America/Sao_Paulo";
}