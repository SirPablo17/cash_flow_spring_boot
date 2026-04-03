package pablo.nasc.cash_flow_spring_boot.services.debt;

import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface de leitura para operações de consulta de Debt.
 *
 * Aplicação do Interface Segregation Principle (ISP):
 * controllers que apenas consultam dívidas injetam somente esta interface,
 * sem depender de métodos de escrita que não utilizam.
 */
public interface DebtReadService {

    /**
     * Lista dívidas do usuário com filtros opcionais e paginação.
     * Retorna {@link DebtSummaryResponse} (sem parcelas) para respostas mais leves.
     */
    Page<DebtSummaryResponse> listDebts(Long userId, Boolean active,
                                        Long categoryId, Long tagId,
                                        Pageable pageable);

    /**
     * Retorna os detalhes completos de uma dívida, incluindo parcelas e tags.
     * Valida ownership — lança ForbiddenException se a dívida não pertencer ao usuário.
     */
    DebtResponse getDebt(Long debtId, Long userId);
}