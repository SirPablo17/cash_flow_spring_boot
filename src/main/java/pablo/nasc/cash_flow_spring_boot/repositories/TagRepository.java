package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositório de {@link Tag}.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByName(String name);

    List<Tag> findAllByIdIn(List<Long> ids);

    /**
     * Consulta personalizada — busca tags cujo nome contenha
     * o termo informado (case-insensitive).
     * Usado no endpoint GET /tags/search?name=xyz
     */
    @Query("""
        SELECT t FROM Tag t
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY t.name ASC
    """)
    Page<Tag> searchByName(@Param("name") String name, Pageable pageable);
}