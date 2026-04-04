package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import pablo.nasc.cash_flow_spring_boot.assemblers.UserModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.ChangePasswordRequest;
import pablo.nasc.cash_flow_spring_boot.dto.request.user.UserUpdateRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.user.UserWithTokenResponse;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import pablo.nasc.cash_flow_spring_boot.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuários", description = "Gerenciamento do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserModelAssembler assembler;

    @Operation(
            summary = "Retornar dados do usuário autenticado",
            description = "Retorna os dados do usuário logado com links HATEOAS para navegação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @AuthenticationPrincipal UserDetails principal) {

        UserResponse response = userService.getMe(resolveUserId(principal));
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @Operation(
            summary = "Atualizar dados do usuário",
            description = "Atualiza nome e/ou e-mail do usuário. " +
                    "**Importante:** a resposta contém novos tokens JWT. " +
                    "Se o e-mail for alterado, o token anterior não será mais válido. " +
                    "Substitua os tokens armazenados pelos retornados nesta resposta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados — novos tokens gerados"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "E-mail já está em uso",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/me")
    public ResponseEntity<UserWithTokenResponse> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserUpdateRequest request) {

        UserWithTokenResponse response = userService.update(resolveUserId(principal), request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Alterar senha",
            description = "Altera a senha do usuário autenticado. " +
                    "Exige a senha atual para confirmar a identidade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Senha atual incorreta",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(resolveUserId(principal), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Desativar conta",
            description = "Realiza soft delete da conta do usuário autenticado. " +
                    "O registro permanece no banco com active = false."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta desativada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal UserDetails principal) {

        userService.deactivate(resolveUserId(principal));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }
}