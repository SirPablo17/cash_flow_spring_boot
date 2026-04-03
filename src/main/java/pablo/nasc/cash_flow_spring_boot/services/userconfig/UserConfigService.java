package pablo.nasc.cash_flow_spring_boot.services.userconfig;

import pablo.nasc.cash_flow_spring_boot.dto.request.userconfig.UserConfigUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import pablo.nasc.cash_flow_spring_boot.entities.UserConfig;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.UserConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas operações de configuração do usuário autenticado.
 *
 * Endpoints cobertos:
 *   GET    /users/me/config   → getConfig()
 *   PATCH  /users/me/config   → updateConfig()
 *
 * O UserConfig é criado automaticamente via cascade no registro do User,
 * portanto este serviço nunca cria — apenas lê e atualiza.
 */
@Service
@RequiredArgsConstructor
public class UserConfigService {

    private final UserConfigRepository userConfigRepository;

    /**
     * Retorna as configurações do usuário autenticado.
     */
    @Transactional(readOnly = true)
    public UserConfigResponse getConfig(Long userId) {
        UserConfig config = findByUserId(userId);
        return toResponse(config);
    }

    /**
     * Atualiza parcialmente as configurações do usuário.
     */
    @Transactional
    public UserConfigResponse updateConfig(Long userId, UserConfigUpdateRequest request) {
        UserConfig config = findByUserId(userId);

        config.setPreferredCurrency(request.getPreferredCurrency());
        config.setEnableEmailAlerts(request.getEnableEmailAlerts());
        config.setAlertDaysBeforeDue(request.getAlertDaysBeforeDue());
        config.setTimezone(request.getTimezone());

        return toResponse(userConfigRepository.save(config));
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private UserConfig findByUserId(Long userId) {
        return userConfigRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configurações não encontradas para o usuário de id " + userId
                ));
    }

    private UserConfigResponse toResponse(UserConfig config) {
        return new UserConfigResponse(
                config.getId(),
                config.getPreferredCurrency(),
                config.getEnableEmailAlerts(),
                config.getAlertDaysBeforeDue(),
                config.getTimezone()
        );
    }
}