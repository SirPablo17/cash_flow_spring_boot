package pablo.nasc.cash_flow_spring_boot.dto.request.installment;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para atualização das observações livres de uma parcela.
 * Endpoint: PATCH /api/v1/installments/{id}/notes
 *
 * O campo notes é opcional — pode ser enviado nulo para limpar as observações.
 */
@Getter
@Setter
public class InstallmentNotesRequest {

    @Size(max = 300, message = "As observações devem ter no máximo 300 caracteres")
    private String notes;
}