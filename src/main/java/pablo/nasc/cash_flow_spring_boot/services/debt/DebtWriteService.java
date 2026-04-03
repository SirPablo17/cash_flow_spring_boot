package pablo.nasc.cash_flow_spring_boot.services.debt;

import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtCreateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtResponse;

/**
 * Interface de escrita para operações de mutação de Debt.
 *
 * Aplicação do Interface Segregation Principle (ISP):
 * separada da {@link DebtReadService} para que controllers de leitura
 * não dependam de métodos de escrita desnecessários.
 */
public interface DebtWriteService {

    /** Cria uma dívida e gera automaticamente todas as parcelas. */
    DebtResponse createDebt(DebtCreateRequest request, Long userId);

    /** Atualiza apenas campos informativos (título, descrição, credor). */
    DebtResponse updateDebt(Long debtId, DebtUpdateRequest request, Long userId);

    /** Soft delete: desativa a dívida e cancela parcelas PENDING e OVERDUE. */
    void deleteDebt(Long debtId, Long userId);

    /** Associa uma tag à dívida. */
    DebtResponse addTag(Long debtId, Long tagId, Long userId);

    /** Remove a associação de uma tag com a dívida. */
    void removeTag(Long debtId, Long tagId, Long userId);
}
