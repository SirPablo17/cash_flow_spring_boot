package pablo.nasc.cash_flow_spring_boot.dto.request.debt;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de entrada para criação de uma nova dívida.
 * Endpoint: POST /api/v1/debts
 *
 * Ao receber este DTO, o sistema automaticamente:
 *   1. Valida que a categoria existe e está ativa
 *   2. Calcula e gera todas as parcelas (Installments)
 *   3. Associa as tags informadas em tagIds
 *
 * Exemplo de payload:
 * {
 *   "categoryId": 2,
 *   "title": "Compra do iPhone 16 Pro",
 *   "description": "Comprado na loja Apple com juros de 1,99% a.m.",
 *   "totalAmount": 9999.00,
 *   "totalInstallments": 12,
 *   "startDate": "2025-08-10",
 *   "interestRate": 0.0199,
 *   "creditor": "Apple Store",
 *   "tagIds": [1, 3]
 * }
 */
@Getter
@Setter
public class DebtCreateRequest {

    @NotNull(message = "A categoria é obrigatória")
    private Long categoryId;

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 150, message = "O título deve ter entre 3 e 150 caracteres")
    private String title;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor total deve ser maior que 0.01")
    @Digits(
            integer = 13, fraction = 2,
            message = "O valor total deve ter no máximo 13 dígitos inteiros e 2 decimais"
    )
    private BigDecimal totalAmount;

    @NotNull(message = "O número de parcelas é obrigatório")
    @Min(value = 1,   message = "A dívida deve ter pelo menos 1 parcela")
    @Max(value = 360, message = "O número de parcelas não pode exceder 360")
    private Integer totalInstallments;

    @NotNull(message = "A data de início é obrigatória")
    @FutureOrPresent(message = "A data de início não pode ser no passado")
    private LocalDate startDate;

    /**
     * Taxa de juros mensal em decimal (ex: 0.0199 = 1,99% a.m.).
     * Opcional — nulo ou zero aciona cálculo simples sem juros.
     */
    @DecimalMin(value = "0.0", message = "A taxa de juros não pode ser negativa")
    @DecimalMax(value = "1.0", message = "A taxa de juros não pode exceder 100% ao mês")
    private BigDecimal interestRate;

    @Size(max = 100, message = "O nome do credor deve ter no máximo 100 caracteres")
    private String creditor;

    /**
     * Ids das tags a associar à dívida. Campo opcional.
     */
    private List<Long> tagIds;
}
