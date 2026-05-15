package com.erp.inventory.service;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.inventory.entity.Category;
import com.erp.inventory.entity.Product;
import com.erp.inventory.dto.CreateProductRequest;
import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.dto.UpdateProductRequest;
import com.erp.inventory.repository.CategoryRepository;
import com.erp.inventory.repository.ProductRepository;
import com.erp.purchasing.entity.Supplier;
import com.erp.purchasing.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<ProductDto> findAll(int page, int size, String search, Long categoryId, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (status == null) {
            status = "ACTIVE";
        }
        Boolean isActive = "ACTIVE".equalsIgnoreCase(status);

        Page<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productRepository.searchByIsActive(search, isActive, pageable);
        } else if (categoryId != null) {
            List<Long> categoryIds = getAllDescendantCategoryIds(categoryId);
            products = productRepository.findByCategoryIdInAndIsActive(categoryIds, isActive, pageable);
        } else {
            products = productRepository.findByIsActive(isActive, pageable);
        }

        return PageResponse.from(products.map(this::toDto));
    }

    public ProductDto findById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toDto(product);
    }

    @Transactional
    public ProductDto create(CreateProductRequest request, Long currentUserId, String ipAddress) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("PRODUCT_001", "SKU already exists");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .supplier(supplier)
                .unitPrice(request.getUnitPrice())
                .costPrice(request.getCostPrice())
                .reorderLevel(request.getReorderLevel())
                .reorderQuantity(request.getReorderQuantity())
                .unitOfMeasure(request.getUnitOfMeasure())
                .currentStock(request.getCurrentStock() != null ? request.getCurrentStock() : 0)
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        product = productRepository.save(product);
        log.info("Created product with id: {} and sku: {}", product.getId(), request.getSku());

        auditLogService.log(currentUserId, "CREATE", "Product", product.getId(), null, ipAddress, "Product created");

        return toDto(product);
    }

    @Transactional
    public ProductDto update(Long id, UpdateProductRequest request, Long currentUserId, String ipAddress) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new BusinessException("PRODUCT_001", "SKU already exists");
            }
            product.setSku(request.getSku());
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));
            product.setSupplier(supplier);
        }
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getUnitPrice() != null) product.setUnitPrice(request.getUnitPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getReorderLevel() != null) product.setReorderLevel(request.getReorderLevel());
        if (request.getReorderQuantity() != null) product.setReorderQuantity(request.getReorderQuantity());
        if (request.getUnitOfMeasure() != null) product.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getCurrentStock() != null) product.setCurrentStock(request.getCurrentStock());

        product = productRepository.save(product);
        log.info("Updated product with id: {}", product.getId());

        auditLogService.log(currentUserId, "UPDATE", "Product", product.getId(), null, ipAddress, "Product updated");

        return toDto(product);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setIsActive(false);
        productRepository.save(product);
        log.info("Deactivated product with id: {}", id);

        auditLogService.log(currentUserId, "DELETE", "Product", id, null, ipAddress, "Product deactivated");
    }

    public long countActive() {
        return productRepository.countByIsActive(true);
    }

    public PageResponse<ProductDto> findLowStock(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("currentStock").ascending());
        Page<Product> products = productRepository.findLowStock(true, pageable);
        return PageResponse.from(products.map(this::toDto));
    }

    private List<Long> getAllDescendantCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        collectChildIds(categoryId, ids);
        return ids;
    }

    private void collectChildIds(Long parentId, List<Long> ids) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        for (Category child : children) {
            ids.add(child.getId());
            collectChildIds(child.getId(), ids);
        }
    }

    private ProductDto toDto(Product product) {
        Long categoryId = null;
        String categoryName = null;
        if (product.getCategory() != null && Boolean.TRUE.equals(product.getCategory().getIsActive())) {
            categoryId = product.getCategory().getId();
            categoryName = product.getCategory().getName();
        }

        return ProductDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .unitPrice(product.getUnitPrice())
                .costPrice(product.getCostPrice())
                .reorderLevel(product.getReorderLevel())
                .reorderQuantity(product.getReorderQuantity())
                .unitOfMeasure(product.getUnitOfMeasure())
                .currentStock(product.getCurrentStock())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
