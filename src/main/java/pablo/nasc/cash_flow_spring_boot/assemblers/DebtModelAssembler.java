package pablo.nasc.cash_flow_spring_boot.assemblers;

import pablo.nasc.cash_flow_spring_boot.controllers.ApiRootController;
import pablo.nasc.cash_flow_spring_boot.controllers.DebtController;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.debt.DebtSummaryResponse;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler responsável por adicionar links HATEOAS ao DebtResponse e DebtSummaryResponse.
 *
 * Links sempre presentes:
 * self         → GET    /api/v1/debts/{id}
 * installments → GET    /api/v1/debts/{id}/installments
 * update       → PUT    /api/v1/debts/{id}
 * collection   → GET    /api/v1/debts
 *
 * Links condicionais (apenas se a dívida estiver ativa):
 * delete → DELETE /api/v1/debts/{id}
 */
@Component
public class DebtModelAssembler
        extends RepresentationModelAssemblerSupport<DebtResponse, DebtResponse> {

    private final CategoryModelAssembler categoryAssembler;
    private final TagModelAssembler tagAssembler;
    private final InstallmentModelAssembler installmentAssembler;

    public DebtModelAssembler(CategoryModelAssembler categoryAssembler,
                              TagModelAssembler tagAssembler,
                              InstallmentModelAssembler installmentAssembler) {
        super(DebtController.class, DebtResponse.class);
        this.categoryAssembler = categoryAssembler;
        this.tagAssembler = tagAssembler;
        this.installmentAssembler = installmentAssembler;
    }

    @Override
    public DebtResponse toModel(DebtResponse response) {
        addNestedLinks(response);

        response.add(
                linkTo(methodOn(DebtController.class)
                        .getById(response.getId(), null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(DebtController.class)
                        .listInstallments(response.getId(), null)).withRel("parcelas"),

                linkTo(methodOn(DebtController.class)
                        .update(response.getId(), null, null)).withRel("atualizar"),

                linkTo(methodOn(DebtController.class)
                        .list(null, null, null, null, null, null)).withRel("colecao")
        );

        // Soft delete — só disponível se a dívida ainda estiver ativa
        if (Boolean.TRUE.equals(response.getActive())) {
            response.add(
                    linkTo(methodOn(DebtController.class)
                            .delete(response.getId(), null)).withRel("excluir")
            );
        }

        return response;
    }

    /**
     * Versão para o resumo (listagem paginada).
     * Mesmos links do detalhe completo.
     */
    public DebtSummaryResponse toSummaryModel(DebtSummaryResponse response) {
        addNestedLinks(response);

        response.add(
                linkTo(methodOn(DebtController.class)
                        .getById(response.getId(), null)).withSelfRel(),

                linkTo(methodOn(ApiRootController.class)
                        .index()).withRel("inicio"),

                linkTo(methodOn(DebtController.class)
                        .listInstallments(response.getId(), null)).withRel("parcelas"),

                linkTo(methodOn(DebtController.class)
                        .update(response.getId(), null, null)).withRel("atualizar"),

                linkTo(methodOn(DebtController.class)
                        .list(null, null, null, null, null, null)).withRel("colecao")
        );

        if (Boolean.TRUE.equals(response.getActive())) {
            response.add(
                    linkTo(methodOn(DebtController.class)
                            .delete(response.getId(), null)).withRel("excluir")
            );
        }

        return response;
    }

    private void addNestedLinks(DebtResponse response) {
        if (response.getCategory() != null) {
            categoryAssembler.toModel(response.getCategory());
        }

        if (response.getTags() != null) {
            response.getTags().forEach(tagAssembler::toModel);
        }

        if (response.getInstallments() != null) {
            response.getInstallments().forEach(installmentAssembler::toModel);
        }
    }

    private void addNestedLinks(DebtSummaryResponse response) {
        if (response.getCategory() != null) {
            categoryAssembler.toModel(response.getCategory());
        }

        if (response.getTags() != null) {
            response.getTags().forEach(tagAssembler::toModel);
        }
    }
}
