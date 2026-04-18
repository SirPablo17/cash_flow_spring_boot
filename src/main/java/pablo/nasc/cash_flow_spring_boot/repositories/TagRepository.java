package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de {@link Tag}.
 * Todas as queries filtram por user.id — tags são privadas por usuário.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Lista todas as tags do usuário.
     */
    List<Tag> findAllByUserId(Long userId);

    /**
     * Busca tag pelo id garantindo ownership.
     */
    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    /**
     * Verifica duplicidade de nome dentro do escopo do usuário.
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * Busca múltiplas tags pelos ids garantindo que pertencem ao usuário.
     * Usado ao criar uma Debt — valida que as tags informadas são do usuário.
     */
    List<Tag> findAllByIdInAndUserId(List<Long> ids, Long userId);

    /**
     * Consulta personalizada — busca por nome parcial dentro do escopo do usuário.
     */
    @Query("""
        SELECT t FROM Tag t
        WHERE t.user.id = :userId
          AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY t.name ASC
    """)
    Page<Tag> searchByNameAndUserId(
            @Param("name") String name,
            @Param("userId") Long userId,
            Pageable pageable
    );
}