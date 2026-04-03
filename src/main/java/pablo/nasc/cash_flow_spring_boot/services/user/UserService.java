package pablo.nasc.cash_flow_spring_boot.services.user;

import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.entities.User;
import pablo.nasc.cash_flow_spring_boot.exceptions.BusinessException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas operações do usuário autenticado.
 *
 * Endpoints cobertos:
 *   GET    /users/me              → getMe()
 *   PUT    /users/me              → update()
 *   PATCH  /users/me/password     → changePassword()
 *   DELETE /users/me              → deactivate()
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retorna os dados do usuário autenticado.
     */
    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = findActiveById(userId);
        return toResponse(user);
    }

    /**
     * Atualiza nome e/ou e-mail do usuário.
     * Valida duplicidade de e-mail antes de salvar.
     */
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        User user = findActiveById(userId);

        // Verifica conflito de e-mail apenas se o e-mail mudou
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("O e-mail informado já está em uso.");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return toResponse(userRepository.save(user));
    }

    /**
     * Altera a senha do usuário.
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
     * O registro permanece no banco com active = false.
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
