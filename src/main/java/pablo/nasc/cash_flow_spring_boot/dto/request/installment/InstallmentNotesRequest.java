package pablo.nasc.cash_flow_spring_boot.dto.request.installment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dados para atualização das observações livres de uma parcela")
@Getter
@Setter
public class InstallmentNotesRequest {

    @Schema(
            description = "Observação livre sobre a parcela. Envie null para limpar.",
            example = "Pago via Pix em 05/09/2025",
            maxLength = 300,
            nullable = true
    )
    @Size(max = 300)
    private String notes;
}