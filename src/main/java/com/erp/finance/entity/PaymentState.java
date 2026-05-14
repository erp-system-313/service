package com.erp.finance.entity;

/**
 * Payment state for invoices — tracked on the Move.
 */
public enum PaymentState {
    NOT_PAID,
    IN_PAYMENT,
    PAID,
    PARTIAL,
    REVERSED
}
