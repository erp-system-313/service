package com.erp.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettingsDto {
    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyAddress;
    private String taxNumber;
    private String currency;
    private Integer fiscalYearStart;
    private String timezone;
    private String dateFormat;
}
