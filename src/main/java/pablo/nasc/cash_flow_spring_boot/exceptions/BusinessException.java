package pablo.nasc.cash_flow_spring_boot.exceptions;

/**
 * Lançada quando uma regra de negócio é violada.
 * Mapeada para HTTP 422 Unprocessable Entity pelo {@code GlobalExceptionHandler}.
 *
 * Exemplos de uso:
 *   - Tentar pagar/cancelar uma parcela em estado terminal (PAID ou CANCELED)
 *   - Tentar criar uma dívida em uma categoria inativa
 *   - Tentar alterar a senha com a senha atual incorreta
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}