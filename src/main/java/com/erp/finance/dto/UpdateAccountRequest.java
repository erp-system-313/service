package com.erp.finance.dto;

import com.erp.finance.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountRequest {

    private String name;

    private AccountType type;

    private Long parentId;

    private Boolean isActive;
}