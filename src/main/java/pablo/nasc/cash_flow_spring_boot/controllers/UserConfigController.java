package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.assemblers.UserConfigModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.userconfig.UserConfigUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.userconfig.UserConfigResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.userconfig.UserConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/config")
@RequiredArgsConstructor
public class UserConfigController {

    private final UserConfigService userConfigService;
    private final UserRepository userRepository;
    private final UserConfigModelAssembler assembler;

    @GetMapping
    public ResponseEntity<UserConfigResponse> getConfig(
            @AuthenticationPrincipal UserDetails principal) {
        UserConfigResponse response = userConfigService.getConfig(resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PatchMapping
    public ResponseEntity<UserConfigResponse> updateConfig(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserConfigUpdateRequest request) {
        UserConfigResponse response = userConfigService.updateConfig(resolveUserId(principal), request);
        return ResponseEntity.ok(assembler.toModel(response));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}
