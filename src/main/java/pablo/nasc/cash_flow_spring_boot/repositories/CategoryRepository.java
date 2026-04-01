package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de {@link Category}.
 *
 * Regras relevantes da documentação:
 *   - Listagens padrão retornam apenas categorias ativas.
 *   - Categorias inativas não aceitam novas dívidas → verificado no DebtService.
 *   - Exclusão é soft delete (active = false).
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Lista todas as categorias ativas com suporte a paginação.
     * Usado no endpoint GET /categories.
     */
    Page<Category> findAllByActiveTrue(Pageable pageable);

    /**
     * Busca categoria ativa pelo id.
     * Usado no endpoint GET /categories/{id} e na validação ao criar uma Debt.
     */
    Optional<Category> findByIdAndActiveTrue(Long id);

    /**
     * Verifica se já existe uma categoria com o nome informado (constraint UNIQUE).
     * Usado no POST /categories para retornar HTTP 409 Conflict.
     */
    boolean existsByName(String name);
}