package pablo.nasc.cash_flow_spring_boot.assemblers;

import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.DebtController;
import pablo.nasc.cash_flow_spring_boot.controllers.InstallmentController;
import pablo.nasc.cash_flow_spring_boot.dto.response.installment.InstallmentResponse;
import pablo.nasc.cash_flow_spring_boot.entities.enums.PaymentStatus;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler responsável por adicionar links HATEOAS ao InstallmentResponse.
 *
 * Links sempre presentes:
 *   self  → GET   /api/v1/installments/{id}
 *   notes → PATCH /api/v1/installments/{id}/notes
 *   debt  → GET   /api/v1/debts/{debtId}
 *
 * Links condicionais (apenas se status for PENDING ou OVERDUE):
 *   pay    → PATCH /api/v1/installments/{id}/pay
 *   cancel → PATCH /api/v1/installments/{id}/cancel
 *
 * Parcelas em estado terminal (PAID ou CANCELED) não recebem
 * os links de pay e cancel — comunicando ao cliente que a ação não está disponível.
 */
@Component
public class InstallmentModelAssembler
        extends RepresentationModelAssemblerSupport<InstallmentResponse, InstallmentResponse> {

    public InstallmentModelAssembler() {
        super(InstallmentController.class, InstallmentResponse.class);
    }

    @Override
    public InstallmentResponse toModel(InstallmentResponse response) {
        // Links sempre presentes
        response.add(
                linkTo(methodOn(InstallmentController.class)
                        .getById(response.getId(), null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(InstallmentController.class)
                        .updateNotes(response.getId(), null, null)).withRel("observacoes"),

                linkTo(methodOn(DebtController.class)
                        .getById(response.getDebtId(), null)).withRel("divida")
        );

        // Links condicionais — só disponíveis se não estiver em estado terminal
        if (isActionable(response.getStatus())) {
            response.add(
                    linkTo(methodOn(InstallmentController.class)
                            .pay(response.getId(), null)).withRel("pagar"),

                    linkTo(methodOn(InstallmentController.class)
                            .cancel(response.getId(), null)).withRel("cancelar")
            );
        }

        return response;
    }

    /**
     * Retorna true se a parcela ainda pode ser modificada.
     * PAID e CANCELED são estados terminais — sem ações disponíveis.
     */
    private boolean isActionable(PaymentStatus status) {
        return status == PaymentStatus.PENDING || status == PaymentStatus.OVERDUE;
    }
}
