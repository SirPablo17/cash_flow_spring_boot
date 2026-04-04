package pablo.nasc.cash_flow_spring_boot.services.user;

import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserWithTokenResponse;
import pablo.nasc.cash_flow_spring_boot.entities.User;
import pablo.nasc.cash_flow_spring_boot.exceptions.BusinessException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.infrastructure.JwtUtil;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas operações do usuário autenticado.
 *
 * Endpoints cobertos:
 *   GET    /users/me          → getMe()
 *   PUT    /users/me          → update()
 *   PATCH  /users/me/password → changePassword()
 *   DELETE /users/me          → deactivate()
 *
 * Importante — update() retorna UserWithTokenResponse:
 *   O e-mail é o subject do JWT. Se o e-mail for alterado,
 *   o token antigo se torna inválido pois o subject não bate
 *   com nenhum usuário no banco. Por isso, novos tokens são
 *   sempre gerados e retornados na resposta do PUT /users/me,
 *   independente de o e-mail ter mudado ou não.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── Leitura ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return toResponse(findActiveById(userId));
    }

    // ── Escrita ───────────────────────────────────────────────────────────────

    /**
     * Atualiza nome e/ou e-mail do usuário.
     *
     * Sempre gera um novo par de tokens na resposta.
     * Motivo: se o e-mail mudar, o token atual (que contém o e-mail antigo
     * como subject) não funcionará mais nas próximas requisições.
     * O cliente deve substituir os tokens armazenados pelos retornados aqui.
     */
    @Transactional
    public UserWithTokenResponse update(Long userId, UserUpdateRequest request) {
        User user = findActiveById(userId);

        // Valida duplicidade apenas se o e-mail realmente mudou
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("O e-mail informado já está em uso.");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        // Gera novos tokens com o e-mail atual (novo ou mesmo)
        String accessToken  = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new UserWithTokenResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                accessToken,
                refreshToken
        );
    }

    /**
     * Altera a senha do usuário autenticado.
     * Valida que a senha atual está correta antes de trocar.
     * Retorna void → controller responde com HTTP 204 No Content.
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findActiveById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("A senha atual informada está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Desativa a conta do usuário (soft delete).
     * Retorna void → controller responde com HTTP 204 No Content.
     */
    @Transactional
    public void deactivate(Long userId) {
        User user = findActiveById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private User findActiveById(Long userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}