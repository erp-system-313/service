package com.erp.finance.entity;

/**
 * Reconcile Model type — determines how the model is applied.
 */
public enum ReconcileModelType {
    /** Manual button — user must click to apply. */
    WRITEOFF_BUTTON,
    /** Auto-create counterpart entry. */
    WRITEOFF_SUGGESTION,
    /** Auto-reconcile without user intervention. */
    INVOICE_MATCHING
}
