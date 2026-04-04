package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.assemblers.TagModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.tag.TagRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.tag.TagResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Tag;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.TagRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;
    private final TagModelAssembler assembler;

    @GetMapping
    public ResponseEntity<CollectionModel<TagResponse>> listAll() {
        List<TagResponse> tags = tagRepository.findAll()
                .stream()
                .map(this::toResponse)
                .map(assembler::toModel)
                .toList();

        // CollectionModel adiciona link self para a coleção inteira
        CollectionModel<TagResponse> collection = CollectionModel.of(
                tags,
                linkTo(methodOn(TagController.class).listAll()).withSelfRel()
        );

        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getById(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(tag)));
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        tag.setName(request.getName());
        tag.setColorHex(request.getColorHex());

        return ResponseEntity.ok(assembler.toModel(toResponse(tagRepository.save(tag))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        tagRepository.delete(tag);
        return ResponseEntity.noContent().build();
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getColorHex());
    }
}