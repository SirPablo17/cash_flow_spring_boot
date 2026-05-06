package pablo.nasc.cash_flow_spring_boot.dto.request.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados para criação de uma nova dívida. As parcelas são geradas automaticamente.")
@Getter
@Setter
public class DebtCreateRequest {

    @Schema(description = "ID da categoria do usuário à qual a dívida pertence", example = "1")
    @NotNull(message = "A categoria é obrigatória")
    private Long categoryId;

    @Schema(description = "Título descritivo da dívida", example = "Compra do iPhone 16 Pro", minLength = 3, maxLength = 150)
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 150)
    private String title;

    @Schema(description = "Detalhamento opcional da dívida", example = "Comprado na Apple Store com juros de 1,99% a.m.")
    @Size(max = 500)
    private String description;

    @Schema(description = "Valor total da dívida antes do parcelamento", example = "9999.00", minimum = "0.01")
    @NotNull(message = "O valor total é obrigatório")
    @DecimalMin("0.01")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal totalAmount;

    @Schema(description = "Número de parcelas. Use 1 para pagamento à vista.", example = "12", minimum = "1", maximum = "360")
    @NotNull(message = "O número de parcelas é obrigatório")
    @Min(1) @Max(360)
    private Integer totalInstallments;

    @Schema(description = "Data de vencimento da primeira parcela (não pode ser no passado)", example = "2025-09-01")
    @NotNull(message = "A data de início é obrigatória")
    @FutureOrPresent
    private LocalDate startDate;

    @Schema(
            description = "Taxa de juros mensal em decimal. Nulo ou zero = sem juros (divisão simples). " +
                    "Com valor = cálculo pela Tabela Price.",
            example = "0.0199"
    )
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal interestRate;

    @Schema(description = "Nome do credor ou instituição financeira", example = "Apple Store")
    @Size(max = 100)
    private String creditor;

    @Schema(description = "IDs das tags do usuário para associar à dívida", example = "[1, 3]")
    private List<Long> tagIds;
}