package com.erp.finance.dto;

import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
import com.erp.finance.entity.InternalGroup;
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
    private String type;
    private Long parentId;
    private String parentName;
    private Long groupId;
    private String groupName;
    private Boolean reconcile;
    private Boolean deprecated;
    private Boolean isActive;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static String mapToOldType(InternalGroup group) {
        if (group == null) return "EXPENSE";
        return switch (group) {
            case ASSET -> "ASSET";
            case LIABILITY -> "LIABILITY";
            case EQUITY -> "EQUITY";
            case INCOME -> "INCOME";
            case EXPENSE -> "EXPENSE";
            case OFF_BALANCE -> "EXPENSE";
        };
    }

    public static AccountDto fromEntity(Account account, BigDecimal balance) {
        InternalGroup group = account.getInternalGroup();
        return AccountDto.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .accountType(account.getAccountType())
                .internalGroup(group != null ? group.name() : null)
                .type(mapToOldType(group))
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .parentName(account.getParent() != null ? account.getParent().getName() : null)
                .groupId(account.getGroup() != null ? account.getGroup().getId() : null)
                .groupName(account.getGroup() != null ? account.getGroup().getName() : null)
                .reconcile(account.getReconcile())
                .deprecated(account.getDeprecated())
                .isActive(!Boolean.TRUE.equals(account.getDeprecated()))
                .balance(balance)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
