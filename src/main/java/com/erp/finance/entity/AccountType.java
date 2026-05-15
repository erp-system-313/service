package com.erp.finance.entity;

/**
 * 17 account types matching Odoo's account.account.type selection field.
 * Each type maps to an internal group for financial reporting.
 */
public enum AccountType {
    ASSET_RECEIVABLE,
    ASSET_CASH,
    ASSET_CURRENT,
    ASSET_NON_CURRENT,
    ASSET_PREPAYMENTS,
    ASSET_FIXED,
    LIABILITY_PAYABLE,
    LIABILITY_CREDIT_CARD,
    LIABILITY_CURRENT,
    LIABILITY_NON_CURRENT,
    EQUITY,
    EQUITY_UNAFFECTED,
    INCOME,
    INCOME_OTHER,
    EXPENSE,
    EXPENSE_DEPRECIATION,
    EXPENSE_DIRECT_COST,
    OFF_BALANCE
}
