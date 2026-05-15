package com.erp.finance.entity;

/**
 * Display type for move lines — determines rendering and behavior.
 * PRODUCT: regular invoice/entry line
 * TAX: automatically generated tax line
 * PAYMENT_TERM: receivable/payable line for the total
 * LINE_SECTION: section header (non-accountable)
 * LINE_NOTE: note (non-accountable)
 * DISCOUNT: discount line
 * EPD: early payment discount line
 */
public enum LineDisplayType {
    PRODUCT,
    TAX,
    PAYMENT_TERM,
    LINE_SECTION,
    LINE_NOTE,
    DISCOUNT,
    EPD
}
