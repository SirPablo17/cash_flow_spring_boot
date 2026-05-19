package pablo.nasc.cash_flow_spring_boot.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import pablo.nasc.cash_flow_spring_boot.dto.request.category.CategoryRequest;
import pablo.nasc.cash_flow_spring_boot.dto.response.category.CategoryResponse;
import pablo.nasc.cash_flow_spring_boot.dto.response.common.PaginaResponse;
import pablo.nasc.cash_flow_spring_boot.entities.Category;
import pablo.nasc.cash_flow_spring_boot.exceptions.BusinessException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ConflictException;
import pablo.nasc.cash_flow_spring_boot.exceptions.ResourceNotFoundException;
import pablo.nasc.cash_flow_spring_boot.repositories.CategoryRepository;
import pablo.nasc.cash_flow_spring_boot.repositories.UserRepository;

import java.net.URI;

@Tag(name = "Categorias v2", description = "Gerenciamento de categorias em portugues")
@SecurityRequirement(name = "apiKeyAuth")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v2/categorias")
@RequiredArgsConstructor
public class CategoryV2Controller {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Listar categorias ativas do usuario")
    @GetMapping
    public ResponseEntity<PaginaResponse<CategoryResponse>> listActive(
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject Pageable pageable) {

        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(PaginaResponse.de(
                categoryRepository.findAllByUserIdAndActiveTrue(userId, pageable).map(this::toResponse)
        ));
    }

    @Operation(summary = "Buscar categoria por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "ID da categoria") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);
        Category category = categoryRepository.findByIdAndUserIdAndActiveTrue(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        return ResponseEntity.ok(toResponse(category));
    }

    @Operation(summary = "Buscar categorias por nome")
    @GetMapping("/buscar")
    public ResponseEntity<PaginaResponse<CategoryResponse>> search(
            @Parameter(description = "Termo de busca", required = true)
            @RequestParam("nome") String name,
            @AuthenticationPrincipal UserDetails principal,
            @ParameterObject Pageable pageable) {

        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(PaginaResponse.de(
                categoryRepository.searchByNameAndUserId(name, userId, pageable).map(this::toResponse)
        ));
    }

    @Operation(summary = "Criar categoria")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Long userId = resolveUserId(principal);

        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)) {
            throw new ConflictException("Voce ja possui uma categoria com o nome: " + request.getName());
        }

        var user = userRepository.findByIdAndActiveTrue(userId).orElseThrow();

        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconCode(request.getIconCode());
        category.setActive(true);

        Category saved = categoryRepository.save(category);
        return ResponseEntity
                .created(URI.create("/api/v2/categorias/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Atualizar categoria")
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

        return ResponseEntity.ok(toResponse(categoryRepository.save(category)));
    }

    @Operation(summary = "Desativar categoria")
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
                    "Nao e possivel desativar uma categoria com dividas ativas vinculadas."
            );
        }

        category.setActive(false);
        categoryRepository.save(category);
        return ResponseEntity.noContent().build();
    }

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
