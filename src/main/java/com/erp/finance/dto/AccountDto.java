package com.erp.finance.dto;

import com.erp.finance.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.erp.finance.entity.Account;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private Long id;
    private String code;
    private String name;
    private AccountType type;
    private Long parentId;
    private String parentName;
    private BigDecimal balance;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .type(account.getType())
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .parentName(account.getParent() != null ? account.getParent().getName() : null)
                .balance(account.getBalance())
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}