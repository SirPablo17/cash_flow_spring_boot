package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categorias", description = "Gerenciamento de categorias de despesa")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryModelAssembler assembler;


    @Operation(
            summary = "Listar categorias ativas",
            description = "Retorna todas as categorias ativas com suporte a paginação e ordenação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<CategoryResponse>> listActive(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<CategoryResponse> pagedAssembler) {

        Page<CategoryResponse> page = categoryRepository
                .findAllByActiveTrue(pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(category)));
    }

    @Operation(
            summary = "Criar categoria",
            description = "Cria uma nova categoria de despesa. O nome deve ser único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Nome de categoria já existe",
                    content = @Content(schema = @Schema(hidden = true)))
    })
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

    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza completamente os dados de uma categoria existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconCode(request.getIconCode());

        return ResponseEntity.ok(assembler.toModel(toResponse(categoryRepository.save(category))));
    }

    @Operation(
            summary = "Desativar categoria",
            description = "Realiza soft delete da categoria. " +
                    "Não é possível desativar categorias com dívidas ativas vinculadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Categoria possui dívidas ativas",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria") @PathVariable Long id) {

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