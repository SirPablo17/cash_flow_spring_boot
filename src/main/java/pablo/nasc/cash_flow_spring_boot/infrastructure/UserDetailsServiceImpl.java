package pablo.nasc.cash_flow_spring_boot.infrastructure;

import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementação do {@link UserDetailsService} do Spring Security.
 *
 * Responsabilidade: carregar os dados do usuário a partir do banco
 * para que o Spring Security possa autenticar e autorizar as requisições.
 *
 * Usado em dois momentos:
 *   1. No {@link JwtAuthFilter} — para montar o objeto de autenticação a partir do token
 *   2. No {@link SecurityConfig} — registrado no AuthenticationProvider para o login
 *
 * Só carrega usuários ativos (active = true) — contas desativadas
 * recebem UsernameNotFoundException → HTTP 401.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado ou inativo: " + email
                ));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
