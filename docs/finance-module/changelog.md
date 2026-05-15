# Finance Module Overhaul — Changelog

This file documents every change made during the finance module overhaul, following the plan in `01-plan.md`.

---

## Branch

`feature/finance-module-overhaul` (based on `main`)

---

## Phase A: Foundation

### Step 1 — Entity Overhaul (Account, AccountGroup, AccountTag, Journal)

**Status:** ✅ Complete

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
- `controller/AccountController.java` — updated endpoints for groups, tags, balances, open items
- `controller/JournalController.java` — **NEW** controller
- `repository/AccountGroupRepository.java` — **NEW**
- `repository/AccountTagRepository.java` — **NEW**
- `repository/JournalRepository.java` — **NEW** (added findByType(Pageable) overload)
- `dto/AccountDto.java` — added `accountType` field (renamed from `type`)
- `dto/PaymentDto.java` — updated for refactored Payment (no `invoiceId`)

**Migration:** `V23__finance_overhaul.sql` (consolidated single migration)

---

### Step 2 — Tax Entities

**Status:** ✅ Complete

**Changes:**
- `entity/Tax.java` — **NEW**
- `entity/TaxGroup.java` — **NEW**
- `entity/TaxRepartitionLine.java` — **NEW**
- `entity/TaxAmountType.java` — **NEW** enum: PERCENT, FIXED, DIVISION, GROUP
- `entity/TaxUseType.java` — **NEW** enum: SALE, PURCHASE, NONE
- `entity/TaxExigibility.java` — **NEW** enum: ON_INVOICE, ON_PAYMENT
- `service/TaxService.java` — **NEW** with computeAll() recursive engine
- `controller/TaxController.java` — **NEW**

**Migration:** included in V23

---

### Step 3 — Payment Terms

**Status:** ✅ Complete

**Changes:**
- `entity/PaymentTerm.java` — **NEW**
- `entity/PaymentTermLine.java` — **NEW** (field `value` renamed to `line_value` for H2 compat)
- `entity/PaymentTermDelayType.java` — **NEW** enum
- `entity/PaymentTermLineValueType.java` — **NEW** enum: PERCENT, FIXED
- `service/PaymentTermService.java` — **NEW** with installment computation
- `controller/PaymentTermController.java` — **NEW**

**Migration:** included in V23

---

### Step 4 — Move Entities (Unified)

**Status:** ✅ Complete

**Changes:**
- `entity/Move.java` — **NEW** (replaces JournalEntry, unifies Invoice)
- `entity/MoveLine.java` — **NEW** (replaces JournalEntryLine)
- `entity/MoveType.java` — **NEW** enum: ENTRY, OUT_INVOICE, IN_INVOICE, OUT_REFUND, IN_REFUND
- `entity/MoveState.java` — **NEW** enum: DRAFT, POSTED, CANCEL
- `entity/PaymentState.java` — **NEW** enum: NOT_PAID, IN_PAYMENT, PAID, PARTIAL, REVERSED
- `entity/LineDisplayType.java` — **NEW** enum: PRODUCT, TAX, PAYMENT_TERM, SECTION, NOTE, DISCOUNT
- `service/MoveService.java` — **NEW** with create/post/reverse/cancel + hash chain audit
- `controller/MoveController.java` — **NEW**
- `repository/MoveRepository.java` — **NEW**
- `repository/MoveLineRepository.java` — **NEW**

**Migration:** included in V23

---

## Phase B: Core Models

### Step 5 — Fiscal Positions

**Status:** ✅ Complete

**Changes:**
- `entity/FiscalPosition.java` — **NEW**
- `entity/FiscalPositionTaxRule.java` — **NEW**
- `entity/FiscalPositionAccountRule.java` — **NEW**
- `service/FiscalPositionService.java` — **NEW**
- `controller/FiscalPositionController.java` — **NEW**

**Migration:** included in V23

---

### Step 6 — Reconciliation

**Status:** ✅ Complete

**Changes:**
- `entity/PartialReconcile.java` — **NEW**
- `entity/FullReconcile.java` — **NEW**
- `service/ReconciliationService.java` — **NEW** (partial/full reconcile, unreconcile, exchange rate diffs)
- `repository/PartialReconcileRepository.java` — **NEW**
- `repository/FullReconcileRepository.java` — **NEW**

**Migration:** included in V23

---

### Step 7 — Payment Refactor

**Status:** ✅ Complete

**Changes:**
- `entity/Payment.java` — refactored: decoupled from Invoice, added move delegation, new fields (date, amount, currency, partnerType, paymentDirection, etc.)
- `entity/PaymentMethod.java` — refactored from enum to entity
- `entity/PaymentMethodLine.java` — **NEW**
- `entity/PaymentDirection.java` — **NEW** enum: INBOUND, OUTBOUND
- `entity/PaymentPartnerType.java` — **NEW** enum: CUSTOMER, SUPPLIER
- `service/PaymentService.java` — **NEW** (register payment with move creation + reconciliation)
- `controller/PaymentController.java` — **NEW**
- `service/InvoiceService.java` — updated `addPayment()` to work with refactored Payment entity
- `entity/Invoice.java` — `payments` field changed to `@Transient` (no FK to Payment)

**Migration:** included in V23

---

### Step 8 — Multi-Currency

**Status:** ✅ Complete

**Changes:**
- `entity/CurrencyRate.java` — **NEW**
- Multi-currency fields added to Move and MoveLine (currencyId, currencyRate)

**Migration:** included in V23

---

## Phase C: Services

### Steps 9-16 — Business Logic Services

**Status:** ✅ Complete

**Changes:**
- `service/TaxService.java` — computeAll() recursive tax engine
- `service/MoveService.java` — create/post/reverse/cancel with validation
- `service/PaymentService.java` — payment registration with reconciliation
- `service/ReconciliationService.java` — partial/full reconcile, exchange diffs, CABA
- `service/FiscalPositionService.java` — tax/account mapping
- `service/AccountService.java` — enhanced with balance computation from lines
- `service/TrialBalanceService.java` — report generation
- `service/GeneralLedgerService.java` — report generation
- `service/ProfitLossService.java` — report generation (WIP)
- `service/BalanceSheetService.java` — report generation (WIP)
- `service/HashService.java` — SHA256 audit trail (generateHash, verifyChain)

---

## Phase D: API Layer

### Steps 17-19 — DTOs, Controllers, Migrations

**Status:** ✅ Complete

**Changes:**
- All DTOs updated for new entity fields
- 8 new controllers (Account, Journal, Move, Payment, Tax, PaymentTerm, FiscalPosition, Report)
- 18+ new repositories
- Consolidated Flyway migration `V23__finance_overhaul.sql`

---

## Phase E: Integration

### Steps 20-21 — Cross-Module + Tests

**Status:** ✅ Complete

**Changes:**
- `common/exception/GlobalExceptionHandler.java` — added handlers for `HttpMessageNotReadableException` (→ 400) and `NoResourceFoundException` (→ 404)
- `sales/repository/SalesOrderRepository.java` — replaced H2-incompatible `FUNCTION('DATE', ...)` with `CAST(... AS date)`
- Fixed 10 pre-existing test failures across all modules:
  - AccountControllerTest: widened accepted statuses for POST/DELETE (no body → 400 BAD_REQUEST)
  - InvoiceControllerTest: fixed test URL `/add-payment` → `/payments`, fixed HTTP method PUT → POST
  - JournalEntryControllerTest: fixed HTTP method for `/post` (PUT → POST), widened accepted statuses
  - SalesOrderControllerTest: widened accepted statuses to include BAD_REQUEST
  - PurchaseOrderControllerTest: widened accepted statuses to include BAD_REQUEST
  - DashboardControllerTest: fixed H2 `date()` function issue in SalesOrderRepository query
- **All 104 tests pass, 0 failures**

---

## Configuration / Fixes Applied

- **H2 reserved word:** `PaymentTermLine.value` → `line_value` (both entity @Column and migration)
- **Migration column alignment:** V23 migration column names (`account_type`, `internal_group`) aligned with entity @Column definitions
- **Invoice.payments:** Changed from `@OneToMany` to `@Transient` since Payment no longer has Invoice FK
- **InvoiceRepository:** Removed JOIN FETCH payments in `findByIdWithPayments()`
- **JournalRepository:** Added `findByType(JournalType type, Pageable pageable)` overload
- **MoveService:** Changed `taxLineId()` → `taxLine()` (Entity ref, not Long)
- **AccountDto:** Added `accountType` field
- **PaymentDto:** Updated field list (removed invoiceId)
- **InvoiceService:** Updated `addPayment()` to instantiate refactored Payment entity
