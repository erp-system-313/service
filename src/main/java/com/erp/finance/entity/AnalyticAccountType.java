package com.erp.finance.entity;

/**
 * Analytic Account type — determines how the account affects financial reports.
 */
public enum AnalyticAccountType {
    /** Costs are tracked as expenses. */
    EXPENSE,
    /** Costs are tracked as income/revenue. */
    INCOME,
    /** Neutral — no impact on P&L (used for tracking only). */
    NEUTRAL
}
