package pablo.nasc.cash_flow_spring_boot.controllers;

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

/**
 * Controller de categorias.
 * Todos os endpoints exigem Bearer Token válido.
 * Base URL: /api/v1/categories
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * GET /api/v1/categories
     * Lista todas as categorias ativas com paginação.
     * Query params: page, size, sort (padrão: createdAt,desc)
     * Retorna 200 OK.
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> listActive(Pageable pageable) {
        Page<CategoryResponse> response = categoryRepository
                .findAllByActiveTrue(pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/categories/{id}
     * Retorna uma categoria ativa pelo id.
     * Retorna 200 OK ou 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return ResponseEntity.ok(toResponse(category));
    }

    /**
     * POST /api/v1/categories
     * Cria uma nova categoria.
     * Retorna 201 Created ou 409 Conflict se o nome já existir.
     */
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
                .body(toResponse(categoryRepository.save(category)));
    }

    /**
     * PUT /api/v1/categories/{id}
     * Atualiza completamente uma categoria existente.
     * Retorna 200 OK ou 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconCode(request.getIconCode());

        return ResponseEntity.ok(toResponse(categoryRepository.save(category)));
    }

    /**
     * DELETE /api/v1/categories/{id}
     * Desativa a categoria (soft delete).
     * Retorna 204 No Content ou 422 se houver dívidas ativas vinculadas.
     */
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

    // ── Mapeamento Entity → DTO ───────────────────────────────────────────────

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