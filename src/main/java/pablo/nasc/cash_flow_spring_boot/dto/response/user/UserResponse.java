package pablo.nasc.cash_flow_spring_boot.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

/**
 * Estende RepresentationModel para suportar HATEOAS.
 * Links adicionados pelo UserModelAssembler.
 */
@Getter
@AllArgsConstructor
public class UserResponse extends RepresentationModel<UserResponse> {

    private Long id;
    private String name;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}