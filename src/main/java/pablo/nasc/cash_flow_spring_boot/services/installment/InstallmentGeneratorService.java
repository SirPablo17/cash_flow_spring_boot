package pablo.nasc.cash_flow_spring_boot.services.installment;

import pablo.nasc.cash_flow_spring_boot.entities.Debt;
import pablo.nasc.cash_flow_spring_boot.entities.Installment;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pela geração automática de parcelas a partir de uma Debt.
 *
 * Aplicação do Single Responsibility Principle (SRP):
 * esta classe tem UMA única responsabilidade — gerar parcelas.
 * Nenhum outro serviço possui esta lógica.
 *
 * Aplicação do Liskov Substitution Principle (LSP):
 * trabalha apenas com a interface {@link InstallmentCalculationStrategy},
 * sem instanceof ou casting — qualquer implementação é intercambiável.
 *
 * Regras aplicadas conforme documentação (seção 3.1):
 *   - Sem juros (rate nulo ou zero) → SimpleInstallmentCalculation
 *   - Com juros compostos           → PriceTableInstallmentCalculation
 *   - Parcela k vence em startDate + (k-1) meses
 *   - Todas as parcelas criadas com status = PENDING
 */
@Service
@RequiredArgsConstructor
public class InstallmentGeneratorService {

    private final SimpleInstallmentCalculation simpleCalculation;
    private final PriceTableInstallmentCalculation priceTableCalculation;

    /**
     * Gera a lista completa de parcelas para uma dívida.
     * As parcelas ainda não estão persistidas — serão salvas via cascade pelo DebtService.
     *
     * @param debt dívida recém-criada com todos os campos preenchidos
     * @return lista de parcelas prontas para persistência
     */
    public List<Installment> generate(Debt debt) {
        int n = debt.getTotalInstallments();
        BigDecimal rate = debt.getInterestRate();

        // Escolhe estratégia: com juros (Tabela Price) ou sem juros (divisão simples)
        InstallmentCalculationStrategy strategy = hasInterest(rate)
                ? priceTableCalculation
                : simpleCalculation;

        BigDecimal installmentAmount = strategy.calculate(
                debt.getTotalAmount(),
                n,
                rate != null ? rate : BigDecimal.ZERO
        );

        List<Installment> installments = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            Installment installment = new Installment();
            installment.setDebt(debt);
            installment.setInstallmentNumber(i);
            installment.setAmount(installmentAmount);
            // Parcela 1 vence na startDate; parcela k vence em startDate + (k-1) meses
            installment.setDueDate(debt.getStartDate().plusMonths(i - 1));
            installment.setStatus(PaymentStatus.PENDING);
            installments.add(installment);
        }

        return installments;
    }

    private boolean hasInterest(BigDecimal rate) {
        return rate != null && rate.compareTo(BigDecimal.ZERO) > 0;
    }
}