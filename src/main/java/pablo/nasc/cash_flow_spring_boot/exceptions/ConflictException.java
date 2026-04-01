package pablo.nasc.cash_flow_spring_boot.exceptions;

/**
 * Lançada quando há conflito de unicidade em um recurso.
 * Mapeada para HTTP 409 Conflict pelo {@code GlobalExceptionHandler}.
 *
 * Exemplos de uso:
 *   - E-mail já cadastrado no registro de usuário
 *   - Nome de categoria ou tag já existente
 *   - Tag já associada a uma dívida
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}