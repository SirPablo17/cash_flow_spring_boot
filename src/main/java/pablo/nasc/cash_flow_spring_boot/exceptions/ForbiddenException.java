package pablo.nasc.cash_flow_spring_boot.exceptions;

/**
 * Lançada quando um usuário autenticado tenta acessar ou manipular
 * um recurso que pertence a outro usuário.
 * Mapeada para HTTP 403 Forbidden pelo {@code GlobalExceptionHandler}.
 *
 * Diferença importante:
 *   - HTTP 401 Unauthorized → usuário NÃO está autenticado (token ausente/inválido)
 *   - HTTP 403 Forbidden    → usuário ESTÁ autenticado, mas não tem permissão
 *
 * Exemplo de uso:
 *   - Usuário A tenta acessar GET /debts/{id} de uma dívida do usuário B
 *   - Usuário A tenta pagar uma parcela que pertence ao usuário B
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("Acesso negado: você não tem permissão para acessar este recurso.");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
