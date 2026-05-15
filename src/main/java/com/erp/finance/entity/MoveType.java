package com.erp.finance.entity;

/**
 * Move types — Odoo-style unified model.
 * ENTRY: miscellaneous journal entry
 * OUT_INVOICE: customer invoice
 * IN_INVOICE: vendor bill
 * OUT_REFUND: customer credit note
 * IN_REFUND: vendor credit note
 */
public enum MoveType {
    ENTRY,
    OUT_INVOICE,
    IN_INVOICE,
    OUT_REFUND,
    IN_REFUND
}
