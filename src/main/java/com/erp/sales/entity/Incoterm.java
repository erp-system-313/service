package com.erp.sales.entity;

/**
 * Compatibility placeholder.
 *
 * The canonical Incoterm entity now lives in com.erp.finance.entity.Incoterm.
 * This class is intentionally NOT a JPA entity to avoid mapping the incoterms
 * table twice. New code should import com.erp.finance.entity.Incoterm directly.
 */
@Deprecated(forRemoval = true)
public class Incoterm {
}
