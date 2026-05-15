package com.erp.sales.dto;

import com.erp.sales.entity.Partner;
import com.erp.sales.entity.Partner.PartnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDto {
    private Long id;
    private String name;
    private PartnerType type;
    private Long parentId;
    private String parentName;
    private String email;
    private String phone;
    private String mobile;
    private String website;
    private String taxId;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isActive;
    private BigDecimal creditLimit;
    private Long paymentTermId;
    private Long pricelistId;
    private Long salespersonId;
    private Long teamId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PartnerDto fromEntity(Partner partner) {
        return PartnerDto.builder()
                .id(partner.getId())
                .name(partner.getName())
                .type(partner.getType())
                .parentId(partner.getParent() != null ? partner.getParent().getId() : null)
                .parentName(partner.getParent() != null ? partner.getParent().getName() : null)
                .email(partner.getEmail())
                .phone(partner.getPhone())
                .mobile(partner.getMobile())
                .website(partner.getWebsite())
                .taxId(partner.getTaxId())
                .address(partner.getAddress())
                .city(partner.getCity())
                .state(partner.getState())
                .zipCode(partner.getZipCode())
                .country(partner.getCountry())
                .isActive(partner.getIsActive())
                .creditLimit(partner.getCreditLimit())
                .paymentTermId(partner.getPaymentTermId())
                .pricelistId(partner.getPricelistId())
                .salespersonId(partner.getSalespersonId())
                .teamId(partner.getTeamId())
                .notes(partner.getNotes())
                .createdAt(partner.getCreatedAt())
                .updatedAt(partner.getUpdatedAt())
                .build();
    }
}
