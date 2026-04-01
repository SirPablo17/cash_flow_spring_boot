package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de {@link User}.
 *
 * Convenções de nomenclatura Spring Data JPA:
 *   - findBy...AndActiveTrue → WHERE active = true (soft delete)
 *   - existsBy...           → SELECT COUNT(*) > 0 (verificação rápida sem carregar a entidade)
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário ativo pelo e-mail.
     * Usado pelo {@code UserDetailsServiceImpl} na autenticação JWT.
     */
    Optional<User> findByEmailAndActiveTrue(String email);

    /**
     * Busca usuário ativo pelo id.
     * Usado pelos services para garantir que contas desativadas não operam.
     */
    Optional<User> findByIdAndActiveTrue(Long id);

    /**
     * Verifica se já existe um usuário com o e-mail informado (ativo ou não).
     * Usado no registro para retornar HTTP 409 Conflict.
     */
    boolean existsByEmail(String email);
}