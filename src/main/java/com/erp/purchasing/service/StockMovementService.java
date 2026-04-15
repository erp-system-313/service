package com.erp.purchasing.service;

import com.erp.admin.service.AuditLogService;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.inventory.entity.Product;
import com.erp.inventory.repository.ProductRepository;
import com.erp.purchasing.dto.CreateStockMovementRequest;
import com.erp.purchasing.dto.StockMovementDto;
import com.erp.purchasing.entity.StockMovement;
import com.erp.purchasing.repository.StockMovementRepository;
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
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public PageResponse<StockMovementDto> findAll(int page, int size, Long productId, String type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<StockMovement> movements;
        if (productId != null) {
            movements = stockMovementRepository.findByProductId(productId, pageable);
        } else if (type != null) {
            StockMovement.MovementType movementType = StockMovement.MovementType.valueOf(type.toUpperCase());
            movements = stockMovementRepository.findByType(movementType, pageable);
        } else {
            movements = stockMovementRepository.findAll(pageable);
        }

        return PageResponse.from(movements.map(this::toDto));
    }

    public StockMovementDto findById(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement", id));
        return toDto(movement);
    }

    @Transactional
    public StockMovementDto create(CreateStockMovementRequest request, Long currentUserId, String ipAddress) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .date(request.getDate())
                .notes(request.getNotes())
                .build();

        movement = stockMovementRepository.save(movement);
        log.info("Created stock movement with id: {} and type: {}", movement.getId(), request.getType());

        auditLogService.log(null, "CREATE", "StockMovement", movement.getId(), null, ipAddress, "Stock movement created");

        return toDto(movement);
    }

    public long countByType(String type) {
        StockMovement.MovementType movementType = StockMovement.MovementType.valueOf(type.toUpperCase());
        return stockMovementRepository.countByType(movementType);
    }

    private StockMovementDto toDto(StockMovement movement) {
        return StockMovementDto.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .date(movement.getDate())
                .notes(movement.getNotes())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
