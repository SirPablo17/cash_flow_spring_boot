package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Autenticação", description = "Endpoints públicos para registro, login e renovação de token")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria um novo usuário e retorna um par de tokens JWT. " +
                    "Um UserConfig padrão (BRL, alertas ativos, 3 dias) é criado automaticamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("E-mail já cadastrado: " + request.getEmail());
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
                .status(HttpStatus.CREATED)
                .body(new AuthResponse(
                        jwtUtil.generateAccessToken(user.getEmail()),
                        jwtUtil.generateRefreshToken(user.getEmail())
                ));
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Valida e-mail e senha e retorna um par de tokens JWT (access + refresh)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        return ResponseEntity.ok(new AuthResponse(
                jwtUtil.generateAccessToken(request.getEmail()),
                jwtUtil.generateRefreshToken(request.getEmail())
        ));
    }

    @Operation(
            summary = "Renovar access token",
            description = "Valida o refresh token e emite um novo par de tokens JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/refresh")
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