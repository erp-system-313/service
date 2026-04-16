package com.erp.purchasing.dto;

import com.erp.purchasing.entity.StockMovement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementDto {
    private Long id;
    private Long productId;
    private String productName;
    private StockMovement.MovementType type;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String referenceType;
    private Long referenceId;
    private LocalDate date;
    private String notes;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
