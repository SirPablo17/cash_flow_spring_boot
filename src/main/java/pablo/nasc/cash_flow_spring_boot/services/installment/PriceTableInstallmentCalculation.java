package pablo.nasc.cash_flow_spring_boot.services.installment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Estratégia de cálculo pela Tabela Price (Sistema Francês de Amortização).
 * Usada quando interestRate é maior que zero.
 *
 * Fórmula PMT (Pagamento Mensal):
 *   PMT = PV * [i * (1 + i)^n] / [(1 + i)^n - 1]
 *
 * Onde:
 *   PV = totalAmount    (valor presente / valor financiado)
 *   i  = interestRate   (taxa mensal decimal, ex: 0.0199)
 *   n  = totalInstallments
 *
 * Exemplo:
 *   PV = R$ 9.999,00 | i = 1,99% a.m. (0.0199) | n = 12
 *   PMT ≈ R$ 930,16 por parcela
 *
 * Característica principal: todas as parcelas têm o mesmo valor (PMT constante).
 * A amortização e os juros variam internamente, mas o valor pago é sempre igual.
 */
@Component
public class PriceTableInstallmentCalculation implements InstallmentCalculationStrategy {

    private static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);

    @Override
    public BigDecimal calculate(BigDecimal totalAmount, int n, BigDecimal rate) {
        // (1 + i)^n
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate, MC);
        BigDecimal power = onePlusRate.pow(n, MC);

        // Numerador: i * (1 + i)^n
        BigDecimal numerator = rate.multiply(power, MC);

        // Denominador: (1 + i)^n - 1
        BigDecimal denominator = power.subtract(BigDecimal.ONE, MC);

        // PMT = PV * (numerador / denominador)
        return totalAmount
                .multiply(numerator.divide(denominator, MC))
                .setScale(2, RoundingMode.HALF_UP);
    }
}