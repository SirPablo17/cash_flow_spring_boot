package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositório de {@link Category}.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByActiveTrue(Pageable pageable);

    Optional<Category> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    /**
     * Consulta personalizada — busca categorias ativas cujo nome contenha
     * o termo informado (case-insensitive).
     * Usado no endpoint GET /categories/search?name=xyz
     */
    @Query("""
        SELECT c FROM Category c
        WHERE c.active = true
          AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY c.name ASC
    """)
    Page<Category> searchByName(@Param("name") String name, Pageable pageable);
}