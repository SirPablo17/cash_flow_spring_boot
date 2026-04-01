package pablo.nasc.cash_flow_spring_boot.exceptions;

/**
 * Lançada quando um recurso solicitado não é encontrado no banco de dados.
 * Mapeada para HTTP 404 Not Found pelo {@code GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construtor genérico com mensagem personalizada.
     *
     * @param message mensagem descritiva do erro
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor de conveniência que gera uma mensagem padronizada.
     * Exemplo: "Category com id 5 não encontrada."
     *
     * @param resource nome da entidade (ex: "Category", "Debt")
     * @param id       identificador não encontrado
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " com id " + id + " não encontrado(a).");
    }
}