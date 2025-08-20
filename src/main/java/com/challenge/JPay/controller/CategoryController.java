package com.challenge.JPay.controller;

import com.challenge.JPay.dto.request.CategoryRequestDTO;
import com.challenge.JPay.dto.response.CategoryResponseDTO;
import com.challenge.JPay.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorias", description = "Operações voltadas para entidade Category")
public class CategoryController {

    @Autowired
    private CategoryService service;


    @GetMapping
    @Operation(summary = "Pesquisar todas as categorias com paginação")
    @ApiResponse(responseCode = "200", description = "Categorias retornadas com sucesso")
    public ResponseEntity<Page<CategoryResponseDTO>> getAllCategories(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/categories - Finding categories with pagination: {}", pageable);

        var categories = service.findAll(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    @Operation(summary = "Pesquisar todas categorias ativas com paginação")
    @ApiResponse(responseCode = "200", description = "Categorias ativas retornadas com sucesso")
    public ResponseEntity<Page<CategoryResponseDTO>> getAllActiveCategories(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/categories/active - Finding all active categories with pagination: {}", pageable);

        Page<CategoryResponseDTO> categories = service.findAllActive(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pesquisar categoria por ID", description = "Retrieve a specific category by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria retornada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable Long id) {
        log.info("GET /api/categories/{} - Finding category by id", id);

        CategoryResponseDTO category = service.findById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/search")
    @Operation(summary = "Pesquisar categorias pelo nome", description = "Pesquisa as categorias ordenadas pelo nome e paganidas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories found"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<Page<CategoryResponseDTO>> searchCategoriesByName(
            @Parameter(description = "Nome target da pesquisa", required = true)
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("GET /api/categories/search - Searching categories by name: {} with pagination: {}", name, pageable);

        Page<CategoryResponseDTO> categories = service.findByNameContaining(name, pageable);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @Operation(summary = "Criar nova categortia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na categoria"),
            @ApiResponse(responseCode = "409", description = "Nome da categoria já existe na base de dados")
    })
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Parameter(description = "JSON categoria", required = true)
            @Valid @RequestBody CategoryRequestDTO dto) {
        log.info("POST /api/categories - Creating new category: {}", dto.name());

        CategoryResponseDTO createdCategory = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria ", description = "Atualiza uma categoria existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na categoria"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome da categoria já existe na base de dados")
    })
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated category data", required = true)
            @Valid @RequestBody CategoryRequestDTO requestDTO) {
        log.info("PUT /api/categories/{} - Updating category", id);

        CategoryResponseDTO updatedCategory = service.update(id, requestDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate category", description = "Deactivate a category (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Cannot deactivate category with pending accounts")
    })
    public ResponseEntity<Void> deactivateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        log.info("PATCH /api/categories/{}/deactivate - Deactivating category", id);

        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Permanently delete a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete category with associated accounts")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/categories/{} - Deleting category", id);

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
