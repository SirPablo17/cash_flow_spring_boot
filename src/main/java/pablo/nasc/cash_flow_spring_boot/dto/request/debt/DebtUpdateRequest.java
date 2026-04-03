package pablo.nasc.cash_flow_spring_boot.dto.request.debt;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para atualização de uma dívida existente.
 * Endpoint: PUT /api/v1/debts/{id}
 *
 * Permite atualizar apenas campos informativos.
 * Campos financeiros (totalAmount, totalInstallments, interestRate, startDate)
 * NÃO são atualizáveis — isso evitaria inconsistências com as parcelas já geradas.
 */
@Getter
@Setter
public class DebtUpdateRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 150, message = "O título deve ter entre 3 e 150 caracteres")
    private String title;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String description;

    @Size(max = 100, message = "O nome do credor deve ter no máximo 100 caracteres")
    private String creditor;
}