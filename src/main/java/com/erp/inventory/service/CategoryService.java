package com.erp.inventory.service;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.inventory.entity.Category;
import com.erp.inventory.dto.CategoryDto;
import com.erp.inventory.dto.CreateCategoryRequest;
import com.erp.inventory.dto.UpdateCategoryRequest;
import com.erp.inventory.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<CategoryDto> findAll(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Category> categories;
        if (status != null) {
            Boolean isActive = status.toUpperCase().equals("ACTIVE");
            categories = categoryRepository.findByIsActive(isActive, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        return PageResponse.from(categories.map(this::toDto));
    }

    public CategoryDto findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toDto(category);
    }

    @Transactional
    public CategoryDto create(CreateCategoryRequest request, Long currentUserId, String ipAddress) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("CATEGORY_001", "Category name already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .sortOrder(request.getSortOrder())
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        log.info("Created category with id: {} and name: {}", category.getId(), request.getName());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Category", category.getId(), null, ipAddress, "Category created");

        return toDto(category);
    }

    @Transactional
    public CategoryDto update(Long id, UpdateCategoryRequest request, Long currentUserId, String ipAddress) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new BusinessException("CATEGORY_001", "Category name already exists");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getParentId() != null) category.setParentId(request.getParentId());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());

        category = categoryRepository.save(category);
        log.info("Updated category with id: {}", category.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Category", category.getId(), null, ipAddress, "Category updated");

        return toDto(category);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Deactivated category with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "Category", id, null, ipAddress, "Category deactivated");
    }

    public long countActive() {
        return categoryRepository.countByIsActive(true);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
