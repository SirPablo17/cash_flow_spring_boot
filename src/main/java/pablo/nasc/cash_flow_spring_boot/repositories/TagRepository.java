package pablo.nasc.cash_flow_spring_boot.repositories;

import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório de {@link Tag}.
 *
 * Tags usam hard delete (registro removido fisicamente do banco),
 * diferente das outras entidades que usam soft delete.
 *
 * A relação N:M com Debt é gerenciada pela tabela tb_debts_tags,
 * controlada pelo lado dono (Debt). Ao deletar uma Tag, o Spring/JPA
 * remove automaticamente os registros da tabela de junção (cascade na FK).
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Verifica se já existe uma tag com o nome informado (constraint UNIQUE).
     * Usado no POST /tags para retornar HTTP 409 Conflict.
     */
    boolean existsByName(String name);

    /**
     * Busca múltiplas tags de uma só vez pelos seus ids.
     * Usado ao criar uma Debt para associar as tags informadas no request (tagIds).
     */
    List<Tag> findAllByIdIn(List<Long> ids);
}