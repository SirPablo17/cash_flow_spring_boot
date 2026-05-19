package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pablo.nasc.cash_flow_spring_boot.dto.request.tag.TagRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.common.PaginaResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.TagRepository;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;

import java.net.URI;
import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Etiquetas v2", description = "Gerenciamento de etiquetas em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/etiquetas")
@RequiredArgsConstructor
public class TagV2Controller {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Listar todas as etiquetas do usuario")
    @GetMapping
    public ResponseEntity<List<TagResponse>> listAll(
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        List<TagResponse> tags = tagRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Buscar etiqueta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getById(
            @Parameter(description = "ID da etiqueta") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        return ResponseEntity.ok(toResponse(tag));
    }

    @Operation(summary = "Buscar etiquetas por nome")
    @GetMapping("/buscar")
    public ResponseEntity<PaginaResponse<TagResponse>> search(
            @Parameter(description = "Termo de busca", required = true)
            @RequestParam("nome") String name,
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject Pageable pageable) {

        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(PaginaResponse.de(
                tagRepository.searchByNameAndUserId(name, userId, pageable).map(this::toResponse)
        ));
    }

    @Operation(summary = "Criar etiqueta")
    @PostMapping
    public ResponseEntity<TagResponse> create(
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);

        if (tagRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new ConflictException("Voce ja possui uma etiqueta com o nome: " + request.getName());
        }

        var user = userRepository.findByIdAndActiveTrue(userId).orElseThrow();

        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        Tag saved = tagRepository.save(tag);
        return ResponseEntity
                .created(URI.create("/api/v2/etiquetas/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Atualizar etiqueta")
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @Parameter(description = "ID da etiqueta") @PathVariable Long id,
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        return ResponseEntity.ok(toResponse(tagRepository.save(tag)));
    }

    @Operation(summary = "Excluir etiqueta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da etiqueta") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColorHex());
    }
}
