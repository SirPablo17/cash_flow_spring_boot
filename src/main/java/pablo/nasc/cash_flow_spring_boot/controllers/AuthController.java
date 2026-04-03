package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.dto.request.auth.LoginRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.auth.RefreshTokenRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.auth.RegisterRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.auth.AuthResponse;
import pablo.nasc.cash_flow_spring_boot.entities.User;
import pablo.nasc.cash_flow_spring_boot.entities.UserConfig;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.infrastructure.JwtUtil;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticação — endpoints públicos (sem JWT).
 * Base URL: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/v1/auth/register
     * Registra novo usuário e cria UserConfig padrão via cascade.
     * Retorna 201 Created + par de tokens JWT.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("E-mail já cadastrado: " + request.getEmail());
        }

        // Cria User
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        // Cria UserConfig padrão — cascade persiste junto com o User
        UserConfig config = new UserConfig();
        config.setUser(user);
        user.setUserConfig(config);

        userRepository.save(user);

        String accessToken  = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(accessToken, refreshToken));
    }

    /**
     * POST /api/v1/auth/login
     * Autentica usuário e retorna par de tokens JWT.
     * O AuthenticationManager valida e-mail + senha via DaoAuthenticationProvider.
     * Retorna 200 OK ou 401 Unauthorized (tratado pelo GlobalExceptionHandler).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken  = jwtUtil.generateAccessToken(request.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(request.getEmail());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    /**
     * POST /api/v1/auth/refresh
     * Valida o refresh token e emite um novo par de tokens.
     * Retorna 200 OK ou 401 Unauthorized se o token for inválido/expirado.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtUtil.isValid(request.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email        = jwtUtil.extractEmail(request.getRefreshToken());
        String accessToken  = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}