package pablo.nasc.cash_flow_spring_boot.services.installment;

import pablo.nasc.cash_flow_spring_boot.dto.request.installment.InstallmentNotesRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Installment;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import pablo.nasc.cash_flow_spring_boot.exceptions.BusinessException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço responsável pelas operações individuais de parcelas.
 *
 * Responsabilidades:
 *   - Listagem com filtros
 *   - Registro de pagamento (PATCH /pay)
 *   - Cancelamento (PATCH /cancel)
 *   - Atualização de observações (PATCH /notes)
 *   - Listagem por dívida (GET /debts/{id}/installments)
 *
 * Validação de estado terminal:
 *   Parcelas PAID ou CANCELED não admitem novas transições → HTTP 422.
 *   Delegada ao método {@code Installment.isTerminal()} da própria entidade.
 */
@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl {

    private final InstallmentRepository installmentRepository;

    // ── Leitura ───────────────────────────────────────────────────────────────

    public Page<InstallmentResponse> listByUser(Long userId, PaymentStatus status,
                                                LocalDate start, LocalDate end,
                                                Pageable pageable) {
        return installmentRepository
                .findByUserFilters(userId, status, start, end, pageable)
                .map(this::toResponse);
    }

    public InstallmentResponse getById(Long id, Long userId) {
        return toResponse(findAndValidate(id, userId));
    }

    public List<InstallmentResponse> listByDebt(Long debtId) {
        return installmentRepository
                .findAllByDebtIdOrderByInstallmentNumberAsc(debtId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Escrita ───────────────────────────────────────────────────────────────

    /**
     * Registra pagamento de uma parcela.
     * Etapas (conforme seção 3.3 da documentação):
     *   1. Busca parcela pelo id (404 se não encontrada)
     *   2. Valida ownership via debt.user.id (403 se de outro usuário)
     *   3. Valida que não está em estado terminal (422 se PAID ou CANCELED)
     *   4. Atualiza status = PAID e paymentDate = hoje
     *   5. Persiste e retorna InstallmentResponse atualizado
     */
    @Transactional
    public InstallmentResponse pay(Long id, Long userId) {
        Installment installment = findAndValidate(id, userId);
        validateNotTerminal(installment);

        installment.setStatus(PaymentStatus.PAID);
        installment.setPaymentDate(LocalDate.now());

        return toResponse(installmentRepository.save(installment));
    }

    /**
     * Cancela uma parcela individual.
     * Valida estado terminal antes de cancelar.
     */
    @Transactional
    public InstallmentResponse cancel(Long id, Long userId) {
        Installment installment = findAndValidate(id, userId);
        validateNotTerminal(installment);

        installment.setStatus(PaymentStatus.CANCELED);

        return toResponse(installmentRepository.save(installment));
    }

    /**
     * Atualiza as observações livres de uma parcela.
     * Não valida estado terminal — notas podem ser atualizadas em qualquer status.
     */
    @Transactional
    public InstallmentResponse updateNotes(Long id, Long userId,
                                           InstallmentNotesRequest request) {
        Installment installment = findAndValidate(id, userId);
        installment.setNotes(request.getNotes());
        return toResponse(installmentRepository.save(installment));
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    /**
     * Busca e valida a parcela por id e ownership do usuário em uma só chamada.
     * O repository navega Installment → Debt → User para validar a posse.
     */
    private Installment findAndValidate(Long id, Long userId) {
        return installmentRepository.findByIdAndDebtUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Installment", id));
    }

    /**
     * Valida que a parcela não está em estado terminal.
     * Delega ao método utilitário {@code Installment.isTerminal()}.
     */
    private void validateNotTerminal(Installment installment) {
        if (installment.isTerminal()) {
            throw new BusinessException(
                    "A parcela já se encontra em estado terminal (" +
                            installment.getStatus() + "). Nenhuma alteração é permitida."
            );
        }
    }

    // ── Mapeamento Entity → DTO ───────────────────────────────────────────────

    private InstallmentResponse toResponse(Installment i) {
        return new InstallmentResponse(
                i.getId(),
                i.getInstallmentNumber(),
                i.getAmount(),
                i.getDueDate(),
                i.getPaymentDate(),
                i.getStatus(),
                i.getNotes(),
                i.getDebt().getId()
        );
    }
}
