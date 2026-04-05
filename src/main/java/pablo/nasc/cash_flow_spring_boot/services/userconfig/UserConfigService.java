package pablo.nasc.cash_flow_spring_boot.services.userconfig;

import pablo.nasc.cash_flow_spring_boot.dto.request.userconfig.UserConfigUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigAlertResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigCurrencyResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import pablo.nasc.cash_flow_spring_boot.entities.UserConfig;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.UserConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConfigService {

    private final UserConfigRepository userConfigRepository;

    @Transactional(readOnly = true)
    public UserConfigResponse getConfig(Long userId) {
        return toResponse(findByUserId(userId));
    }

    /**
     * Retorna apenas as configurações de alerta do usuário.
     */
    @Transactional(readOnly = true)
    public UserConfigAlertResponse getAlertConfig(Long userId) {
        UserConfig config = findByUserId(userId);
        return new UserConfigAlertResponse(
                config.getEnableEmailAlerts(),
                config.getAlertDaysBeforeDue(),
                config.getTimezone()
        );
    }

    /**
     * Retorna apenas a moeda preferida do usuário.
     */
    @Transactional(readOnly = true)
    public UserConfigCurrencyResponse getCurrency(Long userId) {
        UserConfig config = findByUserId(userId);
        return new UserConfigCurrencyResponse(config.getPreferredCurrency());
    }

    @Transactional
    public UserConfigResponse updateConfig(Long userId, UserConfigUpdateRequest request) {
        UserConfig config = findByUserId(userId);

        config.setPreferredCurrency(request.getPreferredCurrency());
        config.setEnableEmailAlerts(request.getEnableEmailAlerts());
        config.setAlertDaysBeforeDue(request.getAlertDaysBeforeDue());
        config.setTimezone(request.getTimezone());

        return toResponse(userConfigRepository.save(config));
    }

    /**
     * Restaura as configurações do usuário para os valores padrão do sistema:
     *   preferredCurrency  = BRL
     *   enableEmailAlerts  = true
     *   alertDaysBeforeDue = 3
     *   timezone           = America/Sao_Paulo
     */
    @Transactional
    public UserConfigResponse resetToDefaults(Long userId) {
        findByUserId(userId); // valida que o config existe
        userConfigRepository.resetToDefaults(userId);
        return toResponse(findByUserId(userId));
    }

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