package com.erp.inventory.service;

import com.erp.admin.service.AuditLogService;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public PageResponse<ProductDto> findAll(int page, int size, Long categoryId, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Product> products;
        if (categoryId != null && status != null) {
            Product.Status productStatus = Product.Status.valueOf(status.toUpperCase());
            products = productRepository.findByStatus(productStatus, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else if (status != null) {
            Product.Status productStatus = Product.Status.valueOf(status.toUpperCase());
            products = productRepository.findByStatus(productStatus, pageable);
        } else {
            products = productRepository.findAll(pageable);
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

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .category(category)
                .unitPrice(request.getUnitPrice())
                .costPrice(request.getCostPrice())
                .reorderLevel(request.getReorderLevel())
                .imageUrl(request.getImageUrl())
                .status(Product.Status.ACTIVE)
                .build();

        product = productRepository.save(product);
        log.info("Created product with id: {} and sku: {}", product.getId(), request.getSku());

        auditLogService.log(null, "CREATE", "Product", product.getId(), null, ipAddress, "Product created");

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
        if (request.getUnitPrice() != null) product.setUnitPrice(request.getUnitPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getReorderLevel() != null) product.setReorderLevel(request.getReorderLevel());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());

        product = productRepository.save(product);
        log.info("Updated product with id: {}", product.getId());

        auditLogService.log(null, "UPDATE", "Product", product.getId(), null, ipAddress, "Product updated");

        return toDto(product);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setStatus(Product.Status.INACTIVE);
        productRepository.save(product);
        log.info("Deactivated product with id: {}", id);

        auditLogService.log(null, "DELETE", "Product", id, null, ipAddress, "Product deactivated");
    }

    public long countActive() {
        return productRepository.countByStatus(Product.Status.ACTIVE);
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .unitPrice(product.getUnitPrice())
                .costPrice(product.getCostPrice())
                .reorderLevel(product.getReorderLevel())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
