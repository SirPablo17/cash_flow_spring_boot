package pablo.nasc.cash_flow_spring_boot.controllers;

import pablo.nasc.cash_flow_spring_boot.assemblers.CategoryModelAssembler;
import pablo.nasc.cash_flow_spring_boot.dto.request.category.CategoryRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Category;
import pablo.nasc.cash_flow_spring_boot.exceptions.BusinessException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryModelAssembler assembler;

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> listActive(Pageable pageable) {
        Page<CategoryResponse> response = categoryRepository
                .findAllByActiveTrue(pageable)
                .map(this::toResponse)
                .map(assembler::toModel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(category)));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Já existe uma categoria com o nome: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconCode(request.getIconCode());
        category.setActive(true);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(toResponse(categoryRepository.save(category))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconCode(request.getIconCode());

        return ResponseEntity.ok(assembler.toModel(toResponse(categoryRepository.save(category))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        boolean hasActiveDebts = category.getDebts().stream()
                .anyMatch(d -> Boolean.TRUE.equals(d.getActive()));

        if (hasActiveDebts) {
            throw new BusinessException(
                    "Não é possível desativar uma categoria com dívidas ativas vinculadas."
            );
        }

        category.setActive(false);
        categoryRepository.save(category);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIconCode(),
                category.getActive()
        );
    }
}