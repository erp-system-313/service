package com.erp.purchasing.dto;

import com.erp.purchasing.entity.StockMovement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStockMovementRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Movement type is required")
    private StockMovement.MovementType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Integer previousStock;

    private Integer newStock;

    private String referenceType;

    private Long referenceId;

    private LocalDate date;

    private String notes;
}
