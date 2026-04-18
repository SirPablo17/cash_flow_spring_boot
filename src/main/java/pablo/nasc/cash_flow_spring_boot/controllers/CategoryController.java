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
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categorias", description = "Gerenciamento de categorias privadas do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryModelAssembler assembler;
    private final UserRepository userRepository;

    @Operation(
            summary = "Listar categorias ativas do usuário",
            description = "Retorna apenas as categorias ativas criadas pelo usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<PagedModel<CategoryResponse>> listActive(
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<CategoryResponse> pagedAssembler) {

        Long userId = resolveUserId(principal);
        Page<CategoryResponse> page = categoryRepository
                .findAllByUserIdAndActiveTrue(userId, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Category category = categoryRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        return ResponseEntity.ok(assembler.toModel(toResponse(category)));
    }

    @Operation(
            summary = "Buscar categorias por nome",
            description = "Busca categorias ativas do usuário cujo nome contenha o termo informado " +
                    "(case-insensitive). Ex: /categories/search?name=mora retorna 'Moradia'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca"),
            @ApiResponse(responseCode = "400", description = "Parâmetro name é obrigatório",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<CategoryResponse>> search(
            @Parameter(description = "Termo de busca", required = true)
            @RequestParam String name,
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<CategoryResponse> pagedAssembler) {

        Long userId = resolveUserId(principal);
        Page<CategoryResponse> page = categoryRepository
                .searchByNameAndUserId(name, userId, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(
            summary = "Criar categoria",
            description = "Cria uma nova categoria para o usuário autenticado. " +
                    "O nome deve ser único dentro das categorias do próprio usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Você já possui uma categoria com este nome",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);

        // Unicidade por usuário — não mais global
        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new ConflictException("Você já possui uma categoria com o nome: " + request.getName());
        }

        var user = userRepository.findByIdAndActiveTrue(userId).orElseThrow();

        Category category = new Category();
        category.setUser(user);
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
            description = "Atualiza os dados de uma categoria do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconCode(request.getIconCode());

        return ResponseEntity.ok(assembler.toModel(toResponse(categoryRepository.save(category))));
    }

    @Operation(
            summary = "Desativar categoria",
            description = "Soft delete da categoria do usuário. " +
                    "Não é possível desativar se houver dívidas ativas vinculadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria desativada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou pertence a outro usuário",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Categoria possui dívidas ativas",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Category category = categoryRepository.findByIdAndUserId(id, userId)
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmailAndActiveTrue(principal.getUsername())
                .orElseThrow()
                .getId();
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