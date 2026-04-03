package pablo.nasc.cash_flow_spring_boot.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de saída para os dados do usuário autenticado.
 * Endpoints: POST /auth/register | GET /users/me | PUT /users/me
 *
 * Nunca expõe o campo password — desacoplamento entre entidade JPA e resposta da API.
 */
@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}