package pablo.nasc.cash_flow_spring_boot.services.user;

import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserFinancialSummaryResponse;
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

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return toResponse(findActiveById(userId));
    }

    /**
     * Retorna o resumo financeiro do usuário:
     * total de dívidas, soma dos valores e total de parcelas pendentes.
     */
    @Transactional(readOnly = true)
    public UserFinancialSummaryResponse getFinancialSummary(Long userId) {
        UserRepository.UserFinancialSummary summary =
                userRepository.findFinancialSummaryByUserId(userId);

        return new UserFinancialSummaryResponse(
                summary.getTotalDebts(),
                BigDecimal.valueOf(summary.getTotalAmount()),
                summary.getTotalInstallments(),
                BigDecimal.valueOf(summary.getPendingAmount())
        );
    }

    @Transactional
    public UserWithTokenResponse update(Long userId, UserUpdateRequest request) {
        User user = findActiveById(userId);

        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("O e-mail informado já está em uso.");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return new UserWithTokenResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                jwtUtil.generateAccessToken(user.getEmail()),
                jwtUtil.generateRefreshToken(user.getEmail())
        );
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findActiveById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("A senha atual informada está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deactivate(Long userId) {
        User user = findActiveById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

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