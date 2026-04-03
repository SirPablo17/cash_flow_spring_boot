package pablo.nasc.cash_flow_spring_boot.infrastructure;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Utilitário responsável por toda a lógica JWT da aplicação:
 *   - Geração de access token e refresh token
 *   - Extração do e-mail (subject) de um token
 *   - Validação de token (assinatura + expiração)
 *
 * Configurações via application.properties:
 *   app.jwt.secret              → chave secreta Base64 para assinar os tokens
 *   app.jwt.expiration-ms       → duração do access token  (ex: 86400000 = 24h)
 *   app.jwt.refresh-expiration-ms → duração do refresh token (ex: 604800000 = 7d)
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {

        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // ── Geração ───────────────────────────────────────────────────────────────

    /**
     * Gera um access token de curta duração.
     * Usado no login, registro e refresh.
     *
     * @param email e-mail do usuário autenticado (subject do token)
     * @return JWT assinado
     */
    public String generateAccessToken(String email) {
        return buildToken(email, expirationMs);
    }

    /**
     * Gera um refresh token de longa duração.
     * Enviado junto ao access token — usado apenas em POST /auth/refresh.
     *
     * @param email e-mail do usuário autenticado (subject do token)
     * @return JWT assinado com TTL maior
     */
    public String generateRefreshToken(String email) {
        return buildToken(email, refreshExpirationMs);
    }

    // ── Extração ──────────────────────────────────────────────────────────────

    /**
     * Extrai o e-mail (subject) do token JWT.
     * Lança JwtException se o token for inválido ou expirado.
     *
     * @param token JWT recebido no header Authorization
     * @return e-mail do usuário
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ── Validação ─────────────────────────────────────────────────────────────

    /**
     * Verifica se o token é válido — assinatura correta e não expirado.
     *
     * @param token JWT a validar
     * @return true se válido, false caso contrário
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private String buildToken(String subject, long ttlMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttlMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
