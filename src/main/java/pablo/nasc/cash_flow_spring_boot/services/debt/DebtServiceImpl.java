package pablo.nasc.cash_flow_spring_boot.services.debt;

import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtCreateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.debt.DebtUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtSummaryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.entities.*;
import pablo.nasc.cash_flow_spring_boot.exceptions.*;
import pablo.nasc.cash_flow_spring_boot.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablo.nasc.cash_flow_spring_boot.services.installment.InstallmentGeneratorService;

import java.util.List;

/**
 * Implementação dos serviços de leitura e escrita de Debt.
 *
 * Aplicação do Dependency Inversion Principle (DIP):
 * depende apenas de interfaces e repositórios injetados via construtor.
 *
 * Aplicação do Interface Segregation Principle (ISP):
 * implementa DebtReadService e DebtWriteService separadamente.
 */
@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtReadService, DebtWriteService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final InstallmentRepository installmentRepository;
    private final InstallmentGeneratorService installmentGeneratorService;

    // ── Leitura ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<DebtSummaryResponse> listDebts(Long userId, Boolean active,
                                               Long categoryId, Long tagId,
                                               Pageable pageable) {
        return debtRepository
                .findByFilters(userId, active, categoryId, tagId, pageable)
                .map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DebtResponse getDebt(Long debtId, Long userId) {
        Debt debt = debtRepository.findByIdAndUserId(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId));
        return toResponse(debt);
    }

    // ── Escrita ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DebtResponse createDebt(DebtCreateRequest request, Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(
                        "Categoria inativa ou inexistente. Não é possível criar dívidas nesta categoria."
                ));

        Debt debt = new Debt();
        debt.setUser(user);
        debt.setCategory(category);
        debt.setTitle(request.getTitle());
        debt.setDescription(request.getDescription());
        debt.setTotalAmount(request.getTotalAmount());
        debt.setTotalInstallments(request.getTotalInstallments());
        debt.setStartDate(request.getStartDate());
        debt.setInterestRate(request.getInterestRate());
        debt.setCreditor(request.getCreditor());

        // Associa tags opcionais
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllByIdIn(request.getTagIds());
            debt.setTags(tags);
        }

        // Gera parcelas automaticamente — persistidas via cascade
        List<Installment> installments = installmentGeneratorService.generate(debt);
        debt.setInstallments(installments);

        return toResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public DebtResponse updateDebt(Long debtId, DebtUpdateRequest request, Long userId) {
        Debt debt = debtRepository.findByIdAndUserIdAndActiveTrue(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId));

        // Apenas campos informativos são atualizáveis — campos financeiros não
        debt.setTitle(request.getTitle());
        debt.setDescription(request.getDescription());
        debt.setCreditor(request.getCreditor());

        return toResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public void deleteDebt(Long debtId, Long userId) {
        Debt debt = debtRepository.findByIdAndUserIdAndActiveTrue(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId));

        // Soft delete na dívida
        debt.setActive(false);
        debtRepository.save(debt);

        // Cancela parcelas PENDING e OVERDUE — preserva PAID (histórico)
        installmentRepository.cancelPendingAndOverdueByDebtId(debtId);
    }

    @Override
    @Transactional
    public DebtResponse addTag(Long debtId, Long tagId, Long userId) {
        Debt debt = debtRepository.findByIdAndUserIdAndActiveTrue(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));

        boolean alreadyLinked = debt.getTags().stream()
                .anyMatch(t -> t.getId().equals(tagId));

        if (alreadyLinked) {
            throw new ConflictException("A tag já está associada a esta dívida.");
        }

        debt.getTags().add(tag);
        return toResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public void removeTag(Long debtId, Long tagId, Long userId) {
        Debt debt = debtRepository.findByIdAndUserIdAndActiveTrue(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", debtId));

        debt.getTags().removeIf(t -> t.getId().equals(tagId));
        debtRepository.save(debt);
    }

    // ── Mapeamento Entity → DTO ───────────────────────────────────────────────

    private DebtResponse toResponse(Debt debt) {
        List<TagResponse> tags = debt.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColorHex()))
                .toList();

        List<InstallmentResponse> installments = debt.getInstallments().stream()
                .map(i -> new InstallmentResponse(
                        i.getId(),
                        i.getInstallmentNumber(),
                        i.getAmount(),
                        i.getDueDate(),
                        i.getPaymentDate(),
                        i.getStatus(),
                        i.getNotes(),
                        debt.getId()
                ))
                .toList();

        CategoryResponse category = new CategoryResponse(
                debt.getCategory().getId(),
                debt.getCategory().getName(),
                debt.getCategory().getDescription(),
                debt.getCategory().getIconCode(),
                debt.getCategory().getActive()
        );

        return new DebtResponse(
                debt.getId(),
                debt.getTitle(),
                debt.getDescription(),
                debt.getTotalAmount(),
                debt.getTotalInstallments(),
                debt.getStartDate(),
                debt.getInterestRate(),
                debt.getCreditor(),
                debt.getActive(),
                debt.getCreatedAt(),
                category,
                tags,
                installments
        );
    }

    private DebtSummaryResponse toSummaryResponse(Debt debt) {
        List<TagResponse> tags = debt.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getColorHex()))
                .toList();

        CategoryResponse category = new CategoryResponse(
                debt.getCategory().getId(),
                debt.getCategory().getName(),
                debt.getCategory().getDescription(),
                debt.getCategory().getIconCode(),
                debt.getCategory().getActive()
        );

        return new DebtSummaryResponse(
                debt.getId(),
                debt.getTitle(),
                debt.getCreditor(),
                debt.getTotalAmount(),
                debt.getTotalInstallments(),
                debt.getStartDate(),
                debt.getActive(),
                debt.getCreatedAt(),
                category,
                tags
        );
    }
}
