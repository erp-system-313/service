package com.erp.sales.dto;

import com.erp.sales.entity.Partner.PartnerType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePartnerRequest {

    @Size(max = 255)
    private String name;

    private PartnerType type;

    private Long parentId;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String mobile;

    @Size(max = 255)
    private String website;

    @Size(max = 50)
    private String taxId;

    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String zipCode;

    @Size(max = 100)
    private String country;

    private Boolean isActive;

    private BigDecimal creditLimit;

    private Long paymentTermId;

    private Long pricelistId;

    private Long salespersonId;

    private Long teamId;

    private String notes;
}
