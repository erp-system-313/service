package com.erp.finance.dto;

import com.erp.finance.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotBlank(message = "Account code is required")
    private String code;

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    private Long parentId;
}