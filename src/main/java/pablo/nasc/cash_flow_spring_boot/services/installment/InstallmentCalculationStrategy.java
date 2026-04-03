package pablo.nasc.cash_flow_spring_boot.services.installment;

import java.math.BigDecimal;

/**
 * Interface de estratégia para cálculo do valor de parcelas.
 *
 * Aplicação do Open/Closed Principle (OCP):
 * novas estratégias de cálculo (ex: SAC, juros simples) podem ser adicionadas
 * criando novas implementações desta interface, sem modificar nenhum código existente.
 *
 * Implementações disponíveis:
 *   {@link SimpleInstallmentCalculation}      → sem juros (divisão simples)
 *   {@link PriceTableInstallmentCalculation}  → Tabela Price / Sistema Francês
 *
 * O {@link InstallmentGeneratorService} escolhe a implementação automaticamente
 * baseado na presença ou ausência de interestRate na Debt.
 */
public interface InstallmentCalculationStrategy {

    /**
     * Calcula o valor de cada parcela.
     *
     * @param totalAmount valor total da dívida
     * @param n           número total de parcelas
     * @param rate        taxa de juros mensal decimal (ex: 0.0199 = 1,99%)
     * @return valor de cada parcela arredondado em 2 casas decimais
     */
    BigDecimal calculate(BigDecimal totalAmount, int n, BigDecimal rate);
}