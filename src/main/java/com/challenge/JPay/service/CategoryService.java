package com.challenge.JPay.service;

import com.challenge.JPay.dto.request.CategoryRequestDTO;
import com.challenge.JPay.dto.response.CategoryResponseDTO;
import com.challenge.JPay.dto.response.CategoryTotalsResponseDTO;
import com.challenge.JPay.exception.BusinessException;
import com.challenge.JPay.exception.CategoryNotFoundException;
import com.challenge.JPay.exception.ResourceDuplicateException;
import com.challenge.JPay.exception.ResourceNotFoundException;
import com.challenge.JPay.model.Category;
import com.challenge.JPay.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        log.info("Finding all categories with pagination: {}", pageable);

        return repository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    public CategoryResponseDTO findMostUsedCategory() {
        log.info("Finding most used category");

        return toResponseDTO(repository.getMostUsedCategory());
    }

    public List<CategoryResponseDTO> findAllActive() {
        log.info("Finding all active categories");

        return repository.findByActiveTrue()
                .stream().map(this::toResponseDTO).toList();
    }

    public List<CategoryTotalsResponseDTO> findTotalAmountByCategory() {
        log.info("Finding totals categories");

        return repository.getCategorieTotals().stream().map((c) -> CategoryTotalsResponseDTO.builder()
                .value(c.getTotalAmount())
                .id(c.getCategory().getId())
                .name(c.getCategory().getName())
                .build()).toList();
    }

    public CategoryResponseDTO findById(Long id) {
        log.info("Finding category by id: {}", id);

        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return toResponseDTO(category);
    }

    public Page<CategoryResponseDTO> findByNameContaining(String name, Pageable pageable) {
        log.info("Finding categories by name containing: {} with pagination: {}", name, pageable);

        return repository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        log.info("Creating new category: {}", dto.name());

        if(repository.existsByNameIgnoreCase(dto.name())) {
            throw new ResourceDuplicateException("Categoria "+ dto.name() + " já existe");
        }

        var createdCategory = repository.save(toEntity(dto));
        log.info("Category created successfully with id: {}", createdCategory.getId());

        return toResponseDTO(createdCategory);
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        log.info("Updating category with id: {}", id);

        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getName().equalsIgnoreCase(dto.name()) &&
                repository.existsByNameIgnoreCase(dto.name())) {
            throw new ResourceDuplicateException("Categoria "+ dto.name() + " já existe");
        }

        category.setName(dto.name());
        category.setDescription(dto.description());

        Category updatedCategory = repository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getId());

        return toResponseDTO(updatedCategory);
    }

    @Transactional
    public void deactivate(Long id) {
        log.info("Deactivating category with id: {}", id);

        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));


        long pendingAccountsCount = repository.countPendingAccountsByCategory(id);
        if (pendingAccountsCount > 0) {
            throw new BusinessException("Não é possivel desativar a categoria pois ela possuí" + pendingAccountsCount + " contas pendentes");
        }

        category.setActive(!category.getActive());
        repository.save(category);
        log.info("Category active status change with successfully with id: {}", id);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting category with id: {}", id);

        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        long accountsCount = repository.countAccountsByCategory(id);
        if (accountsCount > 0) {
            throw new BusinessException("Não é possível deletar a categoria, pois ela possuí " + accountsCount + " contas a pagar associadas");
        }

        repository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }

    private Category toEntity(CategoryRequestDTO dto) {
        return Category.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
    }

    private CategoryResponseDTO toResponseDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

}
