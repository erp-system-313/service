package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.EmailAlias.AliasContactPolicy;
import com.erp.helpdesk.entity.EmailAlias.AliasModelType;
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
public class CreateEmailAliasRequest {
    @NotBlank(message = "Alias local part is required")
    private String aliasLocalPart;

    private String aliasDomain;
    private AliasModelType modelType;
    private String aliasDefaults;
    private AliasContactPolicy aliasContact;
    private Long teamId;
    private Boolean active;
}
