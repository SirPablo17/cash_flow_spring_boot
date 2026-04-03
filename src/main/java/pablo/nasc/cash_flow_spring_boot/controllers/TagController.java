package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.dto.request.tag.TagRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.TagRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de tags.
 * Todos os endpoints exigem Bearer Token válido.
 * Base URL: /api/v1/tags
 *
 * Tags usam hard delete (registro removido fisicamente do banco),
 * diferente das outras entidades que usam soft delete.
 */
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;

    /**
     * GET /api/v1/tags
     * Lista todas as tags cadastradas.
     * Retorna 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> listAll() {
        List<TagResponse> tags = tagRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(tags);
    }

    /**
     * GET /api/v1/tags/{id}
     * Retorna uma tag pelo id.
     * Retorna 200 OK ou 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getById(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        return ResponseEntity.ok(toResponse(tag));
    }

    /**
     * POST /api/v1/tags
     * Cria uma nova tag.
     * Retorna 201 Created ou 409 Conflict se o nome já existir.
     */
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
                .body(toResponse(tagRepository.save(tag)));
    }

    /**
     * PUT /api/v1/tags/{id}
     * Atualiza nome e/ou cor de uma tag existente.
     * Retorna 200 OK ou 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        return ResponseEntity.ok(toResponse(tagRepository.save(tag)));
    }

    /**
     * DELETE /api/v1/tags/{id}
     * Remove a tag permanentemente (hard delete).
     * Registros na tabela de junção tb_debts_tags são removidos automaticamente via FK.
     * Retorna 204 No Content ou 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }

    // ── Mapeamento Entity → DTO ───────────────────────────────────────────────

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColorHex());
    }
}