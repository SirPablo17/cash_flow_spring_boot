package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositório de {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByIdAndActiveTrue(Long id);

    boolean existsByEmail(String email);

    /**
     * Consulta personalizada — retorna um resumo financeiro do usuário:
     * total de dívidas ativas, soma dos valores e total de parcelas pendentes.
     * Usado no endpoint GET /users/me/summary
     */
    @Query("""
        SELECT
            COUNT(d)                                        AS totalDebts,
            COALESCE(SUM(d.totalAmount), 0)                AS totalAmount,
            COUNT(i)                                        AS totalInstallments,
            COALESCE(SUM(CASE WHEN i.status = 'PENDING'
                              THEN i.amount ELSE 0 END), 0) AS pendingAmount
        FROM User u
        LEFT JOIN u.debts d ON d.active = true
        LEFT JOIN d.installments i
        WHERE u.id = :userId
    """)
    UserFinancialSummary findFinancialSummaryByUserId(@Param("userId") Long userId);

    /**
     * Projeção para o resumo financeiro do usuário.
     */
    interface UserFinancialSummary {
        Long getTotalDebts();
        Double getTotalAmount();
        Long getTotalInstallments();
        Double getPendingAmount();
    }
}