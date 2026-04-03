package pablo.nasc.cash_flow_spring_boot.services.installment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Estratégia de cálculo simples — sem juros.
 * Usada quando interestRate é nulo ou zero.
 *
 * Fórmula:
 *   installmentAmount = totalAmount / n
 *
 * Exemplo:
 *   totalAmount = R$ 1.200,00 | n = 12
 *   installmentAmount = R$ 100,00 por parcela
 */
@Component
public class SimpleInstallmentCalculation implements InstallmentCalculationStrategy {

    @Override
    public BigDecimal calculate(BigDecimal totalAmount, int n, BigDecimal rate) {
        return totalAmount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
    }
}
