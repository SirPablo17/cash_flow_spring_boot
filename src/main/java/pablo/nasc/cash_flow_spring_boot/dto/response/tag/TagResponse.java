package pablo.nasc.cash_flow_spring_boot.dto.response.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de saída para tags.
 * Endpoints: GET /tags | GET /tags/{id} | POST /tags | PUT /tags/{id}
 */
@Getter
@AllArgsConstructor
public class TagResponse {

    private Long id;
    private String name;
    private String colorHex;
}
