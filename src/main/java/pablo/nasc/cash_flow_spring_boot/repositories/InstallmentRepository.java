package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Installment;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório de {@link Installment}.
 *
 * Regras relevantes da documentação:
 *   - Usuário acessa apenas parcelas das suas próprias dívidas → HTTP 403.
 *   - Job diário atualiza PENDING → OVERDUE para parcelas vencidas.
 *   - Soft delete de Debt cancela parcelas PENDING e OVERDUE (preserva PAID).
 */
public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    /**
     * Lista todas as parcelas de uma dívida específica, ordenadas pelo número da parcela.
     * Usado no endpoint GET /debts/{id}/installments.
     */
    List<Installment> findAllByDebtIdOrderByInstallmentNumberAsc(Long debtId);

    /**
     * Busca uma parcela pelo id garantindo que pertence ao usuário autenticado.
     * Navega pela associação: Installment → Debt → User.
     * Retorna Optional vazio se o id não existir OU se pertencer a outro usuário.
     * Usado em todos os endpoints de /installments/{id}.
     */
    Optional<Installment> findByIdAndDebtUserId(Long id, Long userId);

    /**
     * Lista parcelas do usuário com filtros opcionais e paginação.
     *
     * Filtros disponíveis (todos opcionais):
     *   - status        → filtra por PaymentStatus (PENDING, PAID, OVERDUE, CANCELED)
     *   - dueDateStart  → parcelas com vencimento a partir desta data
     *   - dueDateEnd    → parcelas com vencimento até esta data
     *
     * Usado no endpoint GET /installments.
     */
    @Query("""
        SELECT i FROM Installment i
        WHERE i.debt.user.id = :userId
          AND (:status IS NULL OR i.status   = :status)
          AND (:start  IS NULL OR i.dueDate >= :start)
          AND (:end    IS NULL OR i.dueDate <= :end)
        ORDER BY i.dueDate ASC
    """)
    Page<Installment> findByUserFilters(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("start")  LocalDate start,
            @Param("end")    LocalDate end,
            Pageable pageable
    );

    /**
     * Job diário — atualiza em lote todas as parcelas PENDING cujo vencimento
     * já passou para o status OVERDUE.
     *
     * Executado pelo {@code InstallmentStatusUpdateService} via @Scheduled.
     * Retorna o número de registros atualizados para fins de log.
     *
     * @Modifying exige @Transactional no caller.
     */
    @Modifying
    @Query("""
        UPDATE Installment i
        SET i.status = 'OVERDUE'
        WHERE i.status = 'PENDING'
          AND i.dueDate < :today
    """)
    int markOverdue(@Param("today") LocalDate today);

    /**
     * Cancela em lote as parcelas PENDING e OVERDUE de uma dívida específica.
     * Chamado pelo soft delete de Debt (DELETE /debts/{id}).
     * Parcelas PAID não são alteradas — histórico de pagamentos preservado.
     *
     * @Modifying exige @Transactional no caller.
     */
    @Modifying
    @Query("""
        UPDATE Installment i
        SET i.status = 'CANCELED'
        WHERE i.debt.id = :debtId
          AND i.status IN ('PENDING', 'OVERDUE')
    """)
    void cancelPendingAndOverdueByDebtId(@Param("debtId") Long debtId);

    @Query("""
        SELECT i FROM Installment i
        JOIN FETCH i.debt d
        LEFT JOIN FETCH d.category
        WHERE d.user.id = :userId
        ORDER BY i.dueDate ASC, i.installmentNumber ASC
    """)
    List<Installment> findAllByUserIdForExport(@Param("userId") Long userId);
}
