package pablo.nasc.cash_flow_spring_boot.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT executado uma vez por requisição.
 *
 * Fluxo de execução:
 *   1. Extrai o token do header Authorization: Bearer {token}
 *   2. Valida o token via JwtUtil
 *   3. Carrega o UserDetails pelo e-mail extraído do token
 *   4. Registra a autenticação no SecurityContext
 *   5. Passa a requisição adiante na filter chain
 *
 * Se o token estiver ausente, inválido ou expirado,
 * o filtro simplesmente não autentica — o Spring Security
 * retorna HTTP 401 automaticamente para rotas protegidas.
 *
 * Estende {@link OncePerRequestFilter} para garantir
 * execução única mesmo em requisições com forward/include.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromHeader(request);

        if (token != null && jwtUtil.isValid(token)) {
            String email = jwtUtil.extractEmail(token);

            // Evita reprocessar se já estiver autenticado no contexto
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization.
     * Formato esperado: "Bearer {token}"
     *
     * @return token sem o prefixo "Bearer ", ou null se ausente/malformado
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
