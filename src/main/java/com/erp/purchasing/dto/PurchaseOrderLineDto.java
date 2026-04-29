package com.erp.purchasing.dto;

import java.math.BigDecimal;

public class PurchaseOrderLineDto {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal lineTotal;
    private Integer receivedQty;
    private String notes;

    public PurchaseOrderLineDto() {}

    public PurchaseOrderLineDto(Long id, Long orderId, Long productId, String productName,
                                Integer quantity, BigDecimal unitPrice, BigDecimal discount,
                                BigDecimal lineTotal, Integer receivedQty, String notes) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.lineTotal = lineTotal;
        this.receivedQty = receivedQty;
        this.notes = notes;
    }

    public static PurchaseOrderLineDtoBuilder builder() {
        return new PurchaseOrderLineDtoBuilder();
    }

    public static class PurchaseOrderLineDtoBuilder {
        private Long id;
        private Long orderId;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal lineTotal;
        private Integer receivedQty;
        private String notes;

        public PurchaseOrderLineDtoBuilder id(Long id) { this.id = id; return this; }
        public PurchaseOrderLineDtoBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public PurchaseOrderLineDtoBuilder productId(Long productId) { this.productId = productId; return this; }
        public PurchaseOrderLineDtoBuilder productName(String productName) { this.productName = productName; return this; }
        public PurchaseOrderLineDtoBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public PurchaseOrderLineDtoBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public PurchaseOrderLineDtoBuilder discount(BigDecimal discount) { this.discount = discount; return this; }
        public PurchaseOrderLineDtoBuilder lineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; return this; }
        public PurchaseOrderLineDtoBuilder totalPrice(BigDecimal totalPrice) { this.lineTotal = totalPrice; return this; }
        public PurchaseOrderLineDtoBuilder receivedQty(Integer receivedQty) { this.receivedQty = receivedQty; return this; }
        public PurchaseOrderLineDtoBuilder notes(String notes) { this.notes = notes; return this; }

        public PurchaseOrderLineDto build() {
            return new PurchaseOrderLineDto(id, orderId, productId, productName, quantity, unitPrice,
                    discount, lineTotal, receivedQty, notes);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public Integer getReceivedQty() { return receivedQty; }
    public void setReceivedQty(Integer receivedQty) { this.receivedQty = receivedQty; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
