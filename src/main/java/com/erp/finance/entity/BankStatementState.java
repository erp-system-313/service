package com.erp.finance.entity;

/**
 * Bank Statement state — follows Odoo's bank statement workflow.
 */
public enum BankStatementState {
    /** Draft — being edited, not yet confirmed. */
    DRAFT,
    /** Open — confirmed, lines can be reconciled. */
    OPEN,
    /** Validated — all lines reconciled, ready to close. */
    VALIDATED,
    /** Reconciled — fully reconciled with bank balance. */
    RECONCILED,
    /** Closed — finalized, no further changes allowed. */
    CLOSED
}
