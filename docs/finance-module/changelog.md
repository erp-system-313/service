# Finance Module Overhaul — Changelog

This file documents every change made during the finance module overhaul, following the plan in `01-plan.md`.

---

## Branch

`feature/finance-module-overhaul` (based on `main`)

---

## Phase A: Foundation

### Step 1 — Entity Overhaul (Account, AccountGroup, AccountTag, Journal)

**Status:** Not started

**Changes:**
- `entity/Account.java` — expanded fields: `accountType` (17 values), `internalGroup`, `reconcile`, `currency`, `tagIds`, `defaultTaxIds`, `group`, `deprecated`, `includeInitialBalance`
- `entity/AccountType.java` — expanded enum: 17 + 1 types
- `entity/InternalGroup.java` — **NEW** enum: EQUITY, ASSET, LIABILITY, INCOME, EXPENSE, OFF_BALANCE
- `entity/AccountGroup.java` — **NEW** entity with code prefix ranges
- `entity/AccountTag.java` — **NEW** entity for tax report tags
- `entity/Journal.java` — **NEW** entity for journals
- `entity/JournalType.java` — **NEW** enum: SALE, PURCHASE, BANK, CASH, GENERAL
- `service/AccountService.java` — updated to handle new fields, compute balances from lines
- `service/JournalService.java` — **NEW** service
- `controller/AccountController.java` — updated endpoints for groups, tags
- `controller/JournalController.java` — **NEW** controller
- `repository/AccountGroupRepository.java` — **NEW**
- `repository/AccountTagRepository.java` — **NEW**
- `repository/JournalRepository.java` — **NEW**

**Migration:** `V23__finance_accounts_journals.sql`

---

### Step 2 — Tax Entities

**Status:** Not started

**Changes:**
- `entity/Tax.java` — **NEW**
- `entity/TaxGroup.java` — **NEW**
- `entity/TaxRepartitionLine.java` — **NEW**
- `entity/TaxAmountType.java` — **NEW** enum: PERCENT, FIXED, DIVISION, GROUP
- `entity/TaxUseType.java` — **NEW** enum: SALE, PURCHASE, NONE
- `entity/TaxExigibility.java` — **NEW** enum: ON_INVOICE, ON_PAYMENT
- `service/TaxService.java` — **NEW** with computeAll() recursive engine
- `controller/TaxController.java` — **NEW**

**Migration:** `V23__finance_taxes.sql` (appended or separate)

---

### Step 3 — Payment Terms

**Status:** Not started

**Changes:**
- `entity/PaymentTerm.java` — **NEW**
- `entity/PaymentTermLine.java` — **NEW**
- `entity/PaymentTermDelayType.java` — **NEW** enum
- `service/PaymentTermService.java` — **NEW** with installment computation
- `controller/PaymentTermController.java` — **NEW**

**Migration:** appended to V23

---

### Step 4 — Move Entities (Unified)

**Status:** Not started

**Changes:**
- `entity/Move.java` — **NEW** (replaces JournalEntry, unifies Invoice)
- `entity/MoveLine.java` — **NEW** (replaces JournalEntryLine)
- `entity/MoveType.java` — **NEW** enum: ENTRY, OUT_INVOICE, IN_INVOICE, OUT_REFUND, IN_REFUND
- `entity/MoveState.java` — **NEW** enum: DRAFT, POSTED, CANCEL
- `entity/PaymentState.java` — **NEW** enum: NOT_PAID, IN_PAYMENT, PAID, PARTIAL, REVERSED
- `entity/LineDisplayType.java` — **NEW** enum: PRODUCT, TAX, PAYMENT_TERM, SECTION, NOTE, DISCOUNT, EPD

**Migration:** `V24__finance_moves.sql`

---

## Phase B: Core Models

### Step 5 — Fiscal Positions

**Status:** Not started

**Changes:**
- `entity/FiscalPosition.java` — **NEW**
- `entity/FiscalPositionTaxRule.java` — **NEW**
- `entity/FiscalPositionAccountRule.java` — **NEW**
- `service/FiscalPositionService.java` — **NEW**

**Migration:** appended to V24 or separate V25

---

### Step 6 — Reconciliation

**Status:** Not started

**Changes:**
- `entity/PartialReconcile.java` — **NEW**
- `entity/FullReconcile.java` — **NEW**
- `service/ReconciliationService.java` — **NEW**
- `repository/PartialReconcileRepository.java` — **NEW**
- `repository/FullReconcileRepository.java` — **NEW**

**Migration:** appended to V24

---

### Step 7 — Payment Refactor

**Status:** Not started

**Changes:**
- `entity/Payment.java` — refactored: decoupled from Invoice, added move delegation
- `entity/PaymentMethod.java` — refactored from enum to entity
- `entity/PaymentMethodLine.java` — **NEW**
- `service/PaymentService.java` — **NEW**
- `controller/PaymentController.java` — **NEW**

**Migration:** appended to V24

---

### Step 8 — Multi-Currency

**Status:** Not started

**Changes:**
- `entity/CurrencyRate.java` — **NEW**
- `service/CurrencyRateService.java` — **NEW**
- Multi-currency fields added to Move and MoveLine

**Migration:** appended to V24

---

## Phase C: Services

### Steps 9-16 — Business Logic Services

**Status:** Not started

**Changes:**
- `service/TaxService.java` — computeAll() recursive tax engine
- `service/MoveService.java` — create/post/reverse/cancel with validation
- `service/PaymentService.java` — payment registration with reconciliation
- `service/ReconciliationService.java` — partial/full reconcile, exchange diffs, CABA
- `service/FiscalPositionService.java` — tax/account mapping
- `service/AccountService.java` — enhanced with balance computation from lines
- `service/TrialBalanceService.java` — report
- `service/GeneralLedgerService.java` — report
- `service/ProfitLossService.java` — report
- `service/BalanceSheetService.java` — report
- `service/HashService.java` — SHA256 audit trail

---

## Phase D: API Layer

### Steps 17-19 — DTOs, Controllers, Migrations

**Status:** Not started

**Changes:**
- All DTOs updated for new entity fields
- All Controllers refactored with new endpoints
- All Repositories updated
- Flyway migrations V23-V25

---

## Phase E: Integration

### Steps 20-21 — Cross-Module + Tests

**Status:** Not started

**Changes:**
- `sales/service/ProductClient.java` — updated for new Move model
- Sales module invoice creation flow → uses MoveService
- Helpdesk integration: link tickets to moves via invoice origin
- All test classes for new services
- Existing test updates for refactored entities
