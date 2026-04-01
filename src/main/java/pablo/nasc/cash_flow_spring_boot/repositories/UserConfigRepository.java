package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de {@link UserConfig}.
 *
 * O {@code UserConfig} é criado automaticamente via cascade ao registrar um {@code User},
 * portanto operações de save/delete direto raramente são necessárias aqui.
 * O repositório é usado principalmente para leitura e atualização parcial (PATCH).
 */
public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {

    /**
     * Busca as configurações pelo id do usuário dono.
     * Usado nos endpoints GET e PATCH /users/me/config.
     */
    Optional<UserConfig> findByUserId(Long userId);
}