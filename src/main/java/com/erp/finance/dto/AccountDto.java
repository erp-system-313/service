package com.erp.finance.dto;

import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
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
public class AccountDto {
    private Long id;
    private String code;
    private String name;
    private AccountType accountType;
    private String internalGroup;
    private Long parentId;
    private String parentName;
    private Long groupId;
    private String groupName;
    private Boolean reconcile;
    private Boolean deprecated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .accountType(account.getAccountType())
                .internalGroup(account.getInternalGroup() != null ? account.getInternalGroup().name() : null)
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .parentName(account.getParent() != null ? account.getParent().getName() : null)
                .groupId(account.getGroup() != null ? account.getGroup().getId() : null)
                .groupName(account.getGroup() != null ? account.getGroup().getName() : null)
                .reconcile(account.getReconcile())
                .deprecated(account.getDeprecated())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
