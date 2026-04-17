package com.erp.purchasing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSupplierRequest {

    @NotNull(message = "Code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String contactPerson;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20)
    private String phone;

    private String address;

    @Size(max = 50)
    private String taxId;

    private Integer paymentTerms;
}
