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

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

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
                .reorderPoint(request.getReorderPoint())
                .reorderQuantity(request.getReorderQuantity())
                .unitOfMeasure(request.getUnitOfMeasure())
                .stockQuantity(0)
                .imageUrl(request.getImageUrl())
                .status(Product.Status.ACTIVE)
                .build();

        product = productRepository.save(product);
        log.info("Created product with id: {} and sku: {}", product.getId(), request.getSku());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Product", product.getId(), null, ipAddress, "Product created");

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
        if (request.getReorderPoint() != null) product.setReorderPoint(request.getReorderPoint());
        if (request.getReorderQuantity() != null) product.setReorderQuantity(request.getReorderQuantity());
        if (request.getUnitOfMeasure() != null) product.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());

        product = productRepository.save(product);
        log.info("Updated product with id: {}", product.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Product", product.getId(), null, ipAddress, "Product updated");

        return toDto(product);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setStatus(Product.Status.INACTIVE);
        productRepository.save(product);
        log.info("Deactivated product with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "Product", id, null, ipAddress, "Product deactivated");
    }

    public long countActive() {
        return productRepository.countByStatus(Product.Status.ACTIVE);
    }

    public PageResponse<ProductDto> findLowStock(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("stockQuantity").ascending());
        Page<Product> products = productRepository.findLowStock(Product.Status.ACTIVE, pageable);
        return PageResponse.from(products.map(this::toDto));
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .unitPrice(product.getUnitPrice())
                .costPrice(product.getCostPrice())
                .reorderPoint(product.getReorderPoint())
                .reorderQuantity(product.getReorderQuantity())
                .unitOfMeasure(product.getUnitOfMeasure())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
