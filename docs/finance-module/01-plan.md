# Finance Module Overhaul — Implementation Plan

## Objective

Transform the current basic finance CRUD into a full double-entry accounting system matching Odoo's capabilities, using the same domain patterns: unified account.move model, journals, tax engine, payment terms, reconciliation, fiscal positions, multi-currency, and audit trail.

## Architecture Pattern

### Odoo-Inspired Layering

```
Controller (REST) → Service (Business Logic) → Entity (JPA) → Repository (Spring Data)
                                            ↕
                                    DTO (Request/Response)
```

### Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Invoice/Move model | **Unified** — Merge Invoice into Move | Odoo's proven pattern; less duplication |
| Balance computation | **Compute from lines** | No drift; always accurate |
| Entity naming | **Follow Odoo** — Move, MoveLine, Journal, Tax, etc. | Easy to reference docs and patterns |
| Payment → Move | **Delegation pattern** — Payment creates its own Move | Clean double-entry for every payment |
| Audit trail | **SHA256 hash chain** per journal | Tamper-evident, regulatory compliant |
| Multi-currency | **Dual amounts** on MoveLine (company + document currency) | Standard double-entry approach |

---

## Entity Model

### Core Entities

```
Move (was JournalEntry + Invoice)
├── id, name, reference, date, state, moveType
├── journal_id → Journal
├── partner_id → Partner (res.partner)
├── currency_id → Currency
├── fiscal_position_id → FiscalPosition
├── payment_term_id → PaymentTerm
├── amount_untaxed, amount_tax, amount_total, amount_residual
├── payment_state: NOT_PAID | IN_PAYMENT | PAID | PARTIAL | REVERSED
├── invoice_date, invoice_date_due, invoice_origin
├── reversed_entry_id → Move (self-ref)
├── inalterable_hash, secure_sequence_number
├── restrict_mode_hash_table
└── lines → [MoveLine]

MoveLine (was JournalEntryLine)
├── id, name, displayType, sequence
├── move_id → Move
├── account_id → Account
├── partner_id → Partner
├── product_id → Product
├── debit, credit, balance (computed)
├── amount_currency, currency_id
├── quantity, price_unit, price_subtotal, price_total, discount
├── tax_ids → [Tax]
├── tax_line_id → Tax (if this IS a tax line)
├── date_maturity
├── amount_residual, reconciled
├── full_reconcile_id → FullReconcile
├── matched_debit_ids → [PartialReconcile]
├── matched_credit_ids → [PartialReconcile]
└── analytic_distribution (JSON)
```

### Configuration Entities

```
Account
├── code (unique per company)
├── name (translatable)
├── accountType (17 types: ASSET_RECEIVABLE, ASSET_CASH, ...)
├── internalGroup (derived from type)
├── reconcile (boolean — for receivable/payable)
├── group_id → AccountGroup
├── tag_ids → [AccountTag]
├── currency_id → Currency
├── default_tax_ids → [Tax]
├── deprecated
└── include_initial_balance

AccountGroup
├── name, code_prefix_from, code_prefix_to
└── parent_id → AccountGroup (self-ref)

AccountTag
├── name
├── applicability (accounts | taxes | both)
└── country_id

Journal
├── name, code (5 chars max)
├── type: SALE | PURCHASE | BANK | CASH | GENERAL
├── default_account_id → Account
├── suspense_account_id → Account
├── currency_id → Currency
├── restrict_mode_hash_table
├── refund_sequence
├── inbound_payment_method_line_ids → [PaymentMethodLine]
├── outbound_payment_method_line_ids → [PaymentMethodLine]
└── alias_id (email for bill upload)
```

### Tax Entities

```
Tax
├── name, typeTaxUse (SALE | PURCHASE | NONE)
├── amountType: PERCENT | FIXED | DIVISION | GROUP
├── amount (rate)
├── price_include (boolean)
├── include_base_amount
├── tax_group_id → TaxGroup
├── children_tax_ids → [Tax] (for group taxes)
├── invoice_repartition_line_ids → [TaxRepartitionLine]
├── refund_repartition_line_ids → [TaxRepartitionLine]
├── tax_exigibility: ON_INVOICE | ON_PAYMENT
├── cash_basis_transition_account_id → Account
├── country_id
└── sequence

TaxGroup
├── name
├── tax_payable_account_id → Account
└── tax_receivable_account_id → Account

TaxRepartitionLine
├── repartition_type: BASE | TAX
├── factor_percent
├── account_id → Account
└── tag_ids → [AccountTag]
```

### Payment & Terms

```
PaymentTerm
├── name
├── line_ids → [PaymentTermLine]
├── note
├── early_discount (boolean)
├── discount_percentage
└── discount_days

PaymentTermLine
├── value: PERCENT | FIXED
├── value_amount
├── delay_type: DAYS_AFTER | END_OF_MONTH | END_OF_NEXT_MONTH
└── nb_days

Payment
├── payment_type: INBOUND | OUTBOUND
├── partner_type: CUSTOMER | SUPPLIER
├── amount, currency_id
├── partner_id → Partner
├── payment_method_line_id → PaymentMethodLine
├── move_id → Move (delegation — this payment IS a move)
├── is_reconciled
├── is_internal_transfer
├── payment_reference
└── reconciled_invoice_ids → [Move] (computed)

PaymentMethod
├── name, code
└── payment_type: INBOUND | OUTBOUND

PaymentMethodLine
├── name
├── payment_method_id → PaymentMethod
├── journal_id → Journal
├── payment_account_id → Account
└── sequence
```

### Reconciliation

```
PartialReconcile
├── debit_move_id → MoveLine
├── credit_move_id → MoveLine
├── full_reconcile_id → FullReconcile
├── amount (company currency)
├── debit_amount_currency
├── credit_amount_currency
├── exchange_move_id → Move (exchange diff entry)
└── max_date

FullReconcile
├── name (matching number)
└── partial_ids → [PartialReconcile]
```

### Fiscal Positions

```
FiscalPosition
├── name
├── tax_ids → [FiscalPositionTaxRule]
├── account_ids → [FiscalPositionAccountRule]
├── country_id
├── country_group_id
└── auto_apply (boolean)

FiscalPositionTaxRule
├── tax_src → Tax
├── tax_dest → Tax
├── country_id
└── country_group_id

FiscalPositionAccountRule
├── account_src → Account
├── account_dest → Account
├── country_id
└── country_group_id
```

### Multi-Currency

```
CurrencyRate
├── currency_id → Currency
├── rate (decimal)
├── company_id
└── date
```

---

## Key Flows

### Invoice Lifecycle

```
DRAFT → POSTED → PAID (via reconciliation)
                 → CANCELLED
                 → REVERSED (credit note)

POST validates:
  1. Lines are balanced (debits = credits)
  2. All accounts belong to the journal
  3. Required fields per display_type are present
  4. Hash computed if restrict_mode_hash_table enabled
```

### Payment Registration

```
1. Select invoices to pay
2. Choose payment method, amount, date
3. System creates:
   a. Move (type=ENTRY) with 2+ lines
      - Line 1: liquidity/debit account (debit)
      - Line 2: receivable/payable account (credit)
   b. Payment record linked to the Move
   c. Reconcile payment line ↔ invoice receivable lines
4. Invoice payment_state updated
```

### Reconciliation

```
1. Select debit line + credit line
2. Create PartialReconcile(amount, debit, credit)
3. Update amount_residual on both lines
4. If amount_residual=0 on all linked lines:
   → Create FullReconcile (sets matching_number)
5. If currencies differ:
   → Create exchange difference entry
6. If cash-basis taxes involved:
   → Create CABA entries
```

### Tax Computation

```
Input: baseLines (from invoice lines)
1. Flatten group taxes to sequence-ordered list
2. If price_include: reverse-compute base from total
3. Iterate forward computing each tax amount
4. Handle rounding globally
5. Return: {totalExcluded, totalIncluded, taxDetails[]}
```

---

## Implementation Order

| Phase | Steps | Description |
|-------|-------|-------------|
| **A: Foundation** | 1-4 | Entities: Account expanded, AccountGroup, AccountTag, Journal, Tax, PaymentTerm |
| **B: Core Models** | 5-8 | Move + MoveLine, FiscalPosition, PartialReconcile, FullReconcile, Payment refactor |
| **C: Services** | 9-16 | TaxService, MoveService, PaymentService, ReconciliationService, Reports, HashService |
| **D: API Layer** | 17-19 | DTOs, Controllers, Flyway migrations |
| **E: Integration** | 20-21 | Cross-module clients, Tests |

---

## Migration Strategy

We will **not** break the existing schema. Instead:

1. **V23**: New tables (journals, taxes, payment_terms, account_groups, account_tags, currency_rates)
2. **V24**: New core tables (moves, move_lines, partial_reconciles, full_reconciles, payments_refactored)
3. **V25**: Fiscal positions, payment methods refactored
4. Old tables (journal_entries, journal_entry_lines, invoices, payments) remain for backward compatibility
5. A data migration step (optional) can copy old data to new schema
6. Old tables are deprecated but not dropped
