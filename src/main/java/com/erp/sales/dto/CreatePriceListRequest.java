package com.erp.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePriceListRequest {

    @NotBlank(message = "Price list name is required")
    @Size(max = 255)
    private String name;

    private Long currencyId;

    private LocalDate validFrom;

    private LocalDate validTo;
}
