package com.erp.sales.dto;

import com.erp.sales.entity.PriceListItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListItemDto {
    private Long id;
    private Long priceListId;
    private Long productId;
    private BigDecimal minQuantity;
    private BigDecimal fixedPrice;
    private BigDecimal discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PriceListItemDto fromEntity(PriceListItem item) {
        return PriceListItemDto.builder()
                .id(item.getId())
                .priceListId(item.getPriceList() != null ? item.getPriceList().getId() : null)
                .productId(item.getProductId())
                .minQuantity(item.getMinQuantity())
                .fixedPrice(item.getFixedPrice())
                .discountPercent(item.getDiscountPercent())
                .validFrom(item.getValidFrom())
                .validTo(item.getValidTo())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
