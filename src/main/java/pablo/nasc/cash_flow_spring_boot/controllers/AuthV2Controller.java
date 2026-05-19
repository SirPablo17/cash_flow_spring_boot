package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.dto.request.auth.LoginRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.auth.RefreshTokenRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.auth.RegisterRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.auth.AuthResponse;
import pablo.nasc.cash_flow_spring_boot.entities.User;
import pablo.nasc.cash_flow_spring_boot.entities.UserConfig;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.infrastructure.JwtUtil;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;

import java.net.URI;

@Tag(name = "Autenticacao v2", description = "Endpoints publicos de autenticacao em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@RestController
@RequestMapping("/api/v2/autenticacao")
@RequiredArgsConstructor
public class AuthV2Controller {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Registrar novo usuario")
    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("E-mail ja cadastrado: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        UserConfig config = new UserConfig();
        config.setUser(user);
        user.setUserConfig(config);

        userRepository.save(user);

        return ResponseEntity
                .created(URI.create("/api/v2/usuarios/eu"))
                .body(new AuthResponse(
                        jwtUtil.generateAccessToken(user.getEmail()),
                        jwtUtil.generateRefreshToken(user.getEmail())
                ));
    }

    @Operation(summary = "Entrar")
    @PostMapping("/entrar")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        return ResponseEntity.ok(new AuthResponse(
                jwtUtil.generateAccessToken(request.getEmail()),
                jwtUtil.generateRefreshToken(request.getEmail())
        ));
    }

    @Operation(summary = "Renovar token de acesso")
    @PostMapping("/renovar-token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtUtil.isValid(request.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtil.extractEmail(request.getRefreshToken());
        return ResponseEntity.ok(new AuthResponse(
                jwtUtil.generateAccessToken(email),
                jwtUtil.generateRefreshToken(email)
        ));
    }
}
