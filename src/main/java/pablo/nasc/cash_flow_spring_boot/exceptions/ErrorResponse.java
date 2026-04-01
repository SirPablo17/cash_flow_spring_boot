package pablo.nasc.cash_flow_spring_boot.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Envelope padrão de resposta de erro da API.
 *
 * Todos os erros retornam este formato JSON, conforme seção 4.9 da documentação.
 *
 * Exemplo — erro de negócio (422):
 * {
 *   "timestamp": "2025-07-10T14:32:00Z",
 *   "status": 422,
 *   "error": "Unprocessable Entity",
 *   "message": "A parcela já se encontra em estado terminal (PAID).",
 *   "path": "/api/v1/installments/47/pay"
 * }
 *
 * Exemplo — erro de validação (400):
 * {
 *   "timestamp": "2025-07-10T14:33:00Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Erro de validação nos campos da requisição.",
 *   "path": "/api/v1/debts",
 *   "fieldErrors": [
 *     { "field": "totalAmount", "rejectedValue": "-100.00", "message": "deve ser maior que 0.01" },
 *     { "field": "startDate",   "rejectedValue": null,      "message": "não deve ser nulo" }
 *   ]
 * }
 *
 * {@code @JsonInclude(NON_NULL)} garante que {@code fieldErrors} não apareça
 * na resposta quando for nulo (erros que não vêm de validação de campos).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Lista de erros por campo — presente apenas em respostas HTTP 400 (Bean Validation).
     * Nulo nos demais tipos de erro.
     */
    private List<FieldErrorDetail> fieldErrors;

    // ── Inner class ───────────────────────────────────────────────────────────

    /**
     * Detalhe de um erro de validação em um campo específico do request body.
     */
    @Getter
    @Builder
    public static class FieldErrorDetail {

        /** Nome do campo que falhou na validação (ex: "totalAmount"). */
        private String field;

        /** Valor rejeitado enviado pelo cliente (pode ser nulo). */
        private Object rejectedValue;

        /** Mensagem descritiva do erro de validação. */
        private String message;
    }
}