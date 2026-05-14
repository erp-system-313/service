package com.erp.sales.entity;

/**
 * Use com.erp.finance.entity.PaymentTerm instead.
 * @deprecated Sales orders now link to the finance module's PaymentTerm entity.
 */
@Deprecated(forRemoval = true)
public enum PaymentTerms {
    NET_30,
    NET_60,
    IMMEDIATE
}