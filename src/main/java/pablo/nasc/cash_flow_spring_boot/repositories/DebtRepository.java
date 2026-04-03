package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Debt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositório de {@link Debt}.
 *
 * Regras relevantes da documentação:
 *   - Usuário acessa apenas suas próprias dívidas → HTTP 403 caso contrário.
 *   - Listagens suportam filtros opcionais: categoryId, tagId, active.
 *   - Exclusão é soft delete (active = false).
 */
public interface DebtRepository extends JpaRepository<Debt, Long> {

    /**
     * Lista dívidas do usuário com filtros opcionais e paginação.
     *
     * Todos os parâmetros de filtro são opcionais:
     *   - active     → null retorna todas (ativas e inativas)
     *   - categoryId → null ignora o filtro de categoria
     *   - tagId      → null ignora o filtro de tag (subquery EXISTS)
     *
     * Usado no endpoint GET /debts.
     */
    @Query("""
        SELECT d FROM Debt d
        WHERE d.user.id = :userId
          AND (:active     IS NULL OR d.active          = :active)
          AND (:categoryId IS NULL OR d.category.id     = :categoryId)
          AND (:tagId      IS NULL OR EXISTS (
                SELECT t FROM d.tags t WHERE t.id = :tagId
          ))
        ORDER BY d.createdAt DESC
    """)
    Page<Debt> findByFilters(
            @Param("userId")     Long userId,
            @Param("active")     Boolean active,
            @Param("categoryId") Long categoryId,
            @Param("tagId")      Long tagId,
            Pageable pageable
    );

    /**
     * Busca uma dívida pelo id garantindo que pertence ao usuário autenticado.
     * Retorna Optional vazio se o id não existir OU se pertencer a outro usuário.
     * Usado para validar ownership antes de exibir detalhes (GET /debts/{id}).
     */
    Optional<Debt> findByIdAndUserId(Long id, Long userId);

    /**
     * Igual ao anterior, mas também exige que a dívida esteja ativa.
     * Usado em operações de escrita (PUT, DELETE) que não devem operar
     * sobre dívidas já canceladas via soft delete.
     */
    Optional<Debt> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}