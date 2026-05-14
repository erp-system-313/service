package com.erp.sales.dto;

import com.erp.sales.entity.PriceList;
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
public class PriceListDto {
    private Long id;
    private String name;
    private Long currencyId;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PriceListDto fromEntity(PriceList priceList) {
        return PriceListDto.builder()
                .id(priceList.getId())
                .name(priceList.getName())
                .currencyId(priceList.getCurrencyId())
                .validFrom(priceList.getValidFrom())
                .validTo(priceList.getValidTo())
                .isActive(priceList.getIsActive())
                .createdAt(priceList.getCreatedAt())
                .updatedAt(priceList.getUpdatedAt())
                .build();
    }
}
