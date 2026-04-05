package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositório de {@link UserConfig}.
 */
public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {

    Optional<UserConfig> findByUserId(Long userId);

    /**
     * Consulta personalizada — restaura as configurações do usuário
     * para os valores padrão do sistema em uma única operação batch.
     * Usado no endpoint POST /users/me/config/reset
     */
    @Modifying
    @Query("""
        UPDATE UserConfig c
        SET c.preferredCurrency  = 'BRL',
            c.enableEmailAlerts  = true,
            c.alertDaysBeforeDue = 3,
            c.timezone           = 'America/Sao_Paulo'
        WHERE c.user.id = :userId
    """)
    void resetToDefaults(@Param("userId") Long userId);
}