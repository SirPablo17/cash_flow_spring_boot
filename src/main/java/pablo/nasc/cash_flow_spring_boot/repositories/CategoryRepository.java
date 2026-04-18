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
 * Todas as queries filtram por user.id — categorias são privadas por usuário.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Lista categorias ativas do usuário.
     */
    Page<Category> findAllByUserIdAndActiveTrue(Long userId, Pageable pageable);

    /**
     * Busca categoria ativa pelo id, garantindo que pertence ao usuário.
     */
    Optional<Category> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    /**
     * Busca categoria pelo id garantindo ownership (ativa ou inativa).
     * Usado em operações de escrita (PUT, DELETE).
     */
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    /**
     * Verifica duplicidade de nome dentro do escopo do usuário.
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * Consulta personalizada — busca por nome parcial dentro do escopo do usuário.
     */
    @Query("""
        SELECT c FROM Category c
        WHERE c.user.id = :userId
          AND c.active = true
          AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY c.name ASC
    """)
    Page<Category> searchByNameAndUserId(
            @Param("name") String name,
            @Param("userId") Long userId,
            Pageable pageable
    );
}