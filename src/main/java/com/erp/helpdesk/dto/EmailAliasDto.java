package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.EmailAlias;
import com.erp.helpdesk.entity.EmailAlias.AliasContactPolicy;
import com.erp.helpdesk.entity.EmailAlias.AliasModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAliasDto {
    private Long id;
    private String aliasLocalPart;
    private String aliasDomain;
    private String fullAlias;
    private AliasModelType modelType;
    private String aliasDefaults;
    private AliasContactPolicy aliasContact;
    private Long teamId;
    private Boolean active;
    private LocalDateTime createdAt;

    public static EmailAliasDto fromEntity(EmailAlias alias) {
        return EmailAliasDto.builder()
                .id(alias.getId())
                .aliasLocalPart(alias.getAliasLocalPart())
                .aliasDomain(alias.getAliasDomain())
                .fullAlias(alias.getFullAlias())
                .modelType(alias.getModelType())
                .aliasDefaults(alias.getAliasDefaults())
                .aliasContact(alias.getAliasContact())
                .teamId(alias.getTeamId())
                .active(alias.getActive())
                .createdAt(alias.getCreatedAt())
                .build();
    }
}
