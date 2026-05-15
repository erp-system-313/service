package com.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.erp.finance.entity.InvoiceLine;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineDto {
    private Long id;
    private Long invoiceId;
    private Long productId;
    private String productName;
    private String productSku;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private Long glAccountId;
    private String taxCode;
    private BigDecimal taxRate;

    public static InvoiceLineDto fromEntity(InvoiceLine line) {
        return InvoiceLineDto.builder()
                .id(line.getId())
                .invoiceId(line.getInvoice() != null ? line.getInvoice().getId() : null)
                .productId(line.getProduct() != null ? line.getProduct().getId() : null)
                .productName(line.getProduct() != null ? line.getProduct().getName() : null)
                .productSku(line.getProduct() != null ? line.getProduct().getSku() : null)
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .lineTotal(line.getLineTotal())
                .glAccountId(line.getGlAccountId())
                .taxCode(line.getTaxCode())
                .taxRate(line.getTaxRate())
                .build();
    }
}
