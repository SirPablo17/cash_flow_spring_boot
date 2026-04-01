package pablo.nasc.cash_flow_spring_boot.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Intercepta todas as exceções lançadas pelos controllers e services,
 * garantindo que a API sempre retorne um JSON amigável no formato {@link ErrorResponse}
 * em vez de um stack trace ou erro 500 genérico.
 *
 * Mapeamento de exceções → HTTP status:
 *   MethodArgumentNotValidException → 400 Bad Request       (Bean Validation)
 *   AuthenticationException         → 401 Unauthorized      (credenciais inválidas)
 *   ForbiddenException              → 403 Forbidden          (acesso negado)
 *   AccessDeniedException           → 403 Forbidden          (Spring Security)
 *   ResourceNotFoundException       → 404 Not Found
 *   ConflictException               → 409 Conflict
 *   BusinessException               → 422 Unprocessable Entity
 *   Exception (genérico)            → 500 Internal Server Error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 — Validação de campos (Bean Validation) ───────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    FieldError fieldError = (FieldError) error;
                    return ErrorResponse.FieldErrorDetail.builder()
                            .field(fieldError.getField())
                            .rejectedValue(fieldError.getRejectedValue())
                            .message(fieldError.getDefaultMessage())
                            .build();
                })
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Erro de validação nos campos da requisição.")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── 401 — Autenticação inválida ───────────────────────────────────────────

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthentication(
            RuntimeException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(build(401, "Unauthorized", "Credenciais inválidas ou token expirado.", request));
    }

    // ── 403 — Acesso negado ───────────────────────────────────────────────────

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(
            RuntimeException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(build(403, "Forbidden", ex.getMessage(), request));
    }

    // ── 404 — Recurso não encontrado ──────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(build(404, "Not Found", ex.getMessage(), request));
    }

    // ── 409 — Conflito de unicidade ───────────────────────────────────────────

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(build(409, "Conflict", ex.getMessage(), request));
    }

    // ── 422 — Violação de regra de negócio ───────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(build(422, "Unprocessable Entity", ex.getMessage(), request));
    }

    // ── 500 — Erro inesperado ─────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        // Log completo interno — stack trace NUNCA exposto na resposta ao cliente
        log.error("Erro inesperado em {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(500, "Internal Server Error",
                        "Ocorreu um erro inesperado. Tente novamente mais tarde.", request));
    }

    // ── Helper privado ────────────────────────────────────────────────────────

    private ErrorResponse build(int status, String error, String message,
                                HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}