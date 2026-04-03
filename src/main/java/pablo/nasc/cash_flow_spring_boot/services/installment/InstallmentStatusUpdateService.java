package pablo.nasc.cash_flow_spring_boot.services.installment;

import pablo.nasc.cash_flow_spring_boot.repositories.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Serviço responsável pela atualização automática de parcelas para OVERDUE.
 *
 * Aplicação do Single Responsibility Principle (SRP):
 * esta classe tem UMA única responsabilidade — executar o job de OVERDUE.
 * Nenhum outro serviço possui esta responsabilidade.
 *
 * Regra de negócio (seção 3.2 da documentação):
 * Atualiza para OVERDUE todas as parcelas que:
 *   - Possuam status = PENDING; E
 *   - Possuam dueDate < LocalDate.now()
 *
 * O job é executado diariamente à meia-noite via @Scheduled(cron).
 * Requer @EnableScheduling na classe principal {@code CashFlowSpringBootApplication}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentStatusUpdateService {

    private final InstallmentRepository installmentRepository;

    /**
     * Job agendado — executa todo dia à meia-noite.
     * Cron: "0 0 0 * * *" → segundo=0, minuto=0, hora=0, qualquer dia/mês/dia-da-semana
     *
     * Usa UPDATE em lote direto no banco (via @Modifying no repository)
     * para evitar carregar todas as entidades em memória.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void markOverdueInstallments() {
        LocalDate today = LocalDate.now();
        int updated = installmentRepository.markOverdue(today);

        log.info("[Scheduler] Job OVERDUE executado em {} — {} parcela(s) atualizada(s).",
                today, updated);
    }
}