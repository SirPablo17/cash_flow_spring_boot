package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import pablo.nasc.cash_flow_spring_boot.assemblers.TagModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.tag.TagRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.TagRepository;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Gerenciamento de etiquetas privadas do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;
    private final TagModelAssembler assembler;
    private final UserRepository userRepository;

    @Operation(
            summary = "Listar todas as tags do usuário",
            description = "Retorna apenas as tags criadas pelo usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<TagResponse>> listAll(
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        List<TagResponse> tags = tagRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .map(assembler::toModel)
                .toList();

        return ResponseEntity.ok(CollectionModel.of(
                tags,
                linkTo(methodOn(TagController.class).listAll(null)).withSelfRel()
        ));
    }

    @Operation(summary = "Buscar tag por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag encontrada"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada ou pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getById(
            @Parameter(description = "ID da tag") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        return ResponseEntity.ok(assembler.toModel(toResponse(tag)));
    }

    @Operation(
            summary = "Buscar tags por nome",
            description = "Busca tags do usuário cujo nome contenha o termo informado " +
                    "(case-insensitive). Ex: /tags/search?name=nubank retorna '@CartaoNubank'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro name é obrigatório",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<TagResponse>> search(
            @Parameter(description = "Termo de busca", required = true)
            @RequestParam String name,
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<TagResponse> pagedAssembler) {

        Long userId = resolveUserId(principal);
        Page<TagResponse> page = tagRepository
                .searchByNameAndUserId(name, userId, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(
            summary = "Criar tag",
            description = "Cria uma nova etiqueta para o usuário autenticado. " +
                    "O nome deve ser único dentro das tags do próprio usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Você já possui uma tag com este nome",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<TagResponse> create(
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);

        // Unicidade por usuário — não mais global
        if (tagRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new ConflictException("Você já possui uma tag com o nome: " + request.getName());
        }

        var user = userRepository.findByIdAndActiveTrue(userId).orElseThrow();

        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        Tag saved = tagRepository.save(tag);
        URI location = linkTo(methodOn(TagController.class).getById(saved.getId(), null)).toUri();

        return ResponseEntity
                .created(location)
                .body(assembler.toModel(toResponse(saved)));
    }

    @Operation(summary = "Atualizar tag")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag atualizada"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada ou pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @Parameter(description = "ID da tag") @PathVariable Long id,
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        return ResponseEntity.ok(assembler.toModel(toResponse(tagRepository.save(tag))));
    }

    @Operation(
            summary = "Deletar tag",
            description = "Remove permanentemente uma tag do usuário (hard delete)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag removida"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada ou pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da tag") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Tag tag = tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColorHex());
    }
}
