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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Gerenciamento de etiquetas para organização de dívidas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;
    private final TagModelAssembler assembler;

    @Operation(summary = "Listar todas as tags")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<TagResponse>> listAll() {
        List<TagResponse> tags = tagRepository.findAll()
                .stream()
                .map(this::toResponse)
                .map(assembler::toModel)
                .toList();

        return ResponseEntity.ok(CollectionModel.of(
                tags,
                linkTo(methodOn(TagController.class).listAll()).withSelfRel()
        ));
    }

    @Operation(summary = "Buscar tag por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag encontrada"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getById(
            @Parameter(description = "ID da tag") @PathVariable Long id) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(tag)));
    }

    @Operation(
            summary = "Buscar tags por nome",
            description = "Consulta personalizada — busca tags cujo nome contenha o termo informado " +
                    "(case-insensitive, busca parcial). " +
                    "Ex: /tags/search?name=nubank retorna '@CartaoNubank'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro name é obrigatório",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<TagResponse>> search(
            @Parameter(description = "Termo de busca no nome da tag", required = true)
            @RequestParam String name,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<TagResponse> pagedAssembler) {

        Page<TagResponse> page = tagRepository
                .searchByName(name, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(
            summary = "Criar tag",
            description = "Cria uma nova etiqueta. O nome deve ser único e seguir o padrão: " +
                    "letras, números, #, @ e hífen (ex: #Urgente, @CartaoNubank)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Nome já existe",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new ConflictException("Já existe uma tag com o nome: " + request.getName());
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(toResponse(tagRepository.save(tag))));
    }

    @Operation(summary = "Atualizar tag")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag atualizada"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @Parameter(description = "ID da tag") @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        return ResponseEntity.ok(assembler.toModel(toResponse(tagRepository.save(tag))));
    }

    @Operation(
            summary = "Deletar tag",
            description = "Remove permanentemente uma tag (hard delete). " +
                    "As associações com dívidas são removidas automaticamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag removida"),
            @ApiResponse(responseCode = "404", description = "Tag não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da tag") @PathVariable Long id) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColorHex());
    }
}