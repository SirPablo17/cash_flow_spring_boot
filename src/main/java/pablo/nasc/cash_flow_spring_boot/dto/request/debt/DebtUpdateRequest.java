package pablo.nasc.cash_flow_spring_boot.dto.request.debt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Schema(
        description = "Dados para atualização de uma dívida. " +
                "Apenas campos informativos são atualizáveis — " +
                "campos financeiros não podem ser alterados para preservar as parcelas geradas."
)
@Getter
@Setter
public class DebtUpdateRequest {

    @Schema(description = "Novo título da dívida", example = "Notebook Dell Atualizado", minLength = 3, maxLength = 150)
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 150)
    private String title;

    @Schema(description = "Nova descrição da dívida", example = "Comprado na Amazon Brasil")
    @Size(max = 500)
    private String description;

    @Schema(description = "Nome atualizado do credor", example = "Amazon Brasil")
    @Size(max = 100)
    private String creditor;
}