# Finance Module Overhaul — Implementation Log

## 2026-05-13

### Session 1: Project Setup

- Created branch `feature/finance-module-overhaul` from `main`
- Created `docs/finance-module/` directory with:
  - `01-plan.md` — Full implementation plan with architecture, entities, flows, migration strategy
  - `changelog.md` — Running log of all changes made during the project
  - `work-log.md` — This file, session-by-session log

### Current State

The current finance module has basic CRUD for:
- `Account` (5 types, stored balance)
- `JournalEntry` + `JournalEntryLine` (simple double-entry)
- `Invoice` (separate from JE, flat tax, linked to Customer)
- `Payment` (tied to Invoice via FK, simple payment methods enum)
- No Journal, Tax, PaymentTerm, FiscalPosition, or Reconciliation entities

### Goal

Refactor to Odoo-inspired model:
- Unified `Move`/`MoveLine` (replaces both JournalEntry and Invoice)
- `Journal` entity with types and sequences
- `Tax` + `TaxGroup` + `TaxRepartitionLine` with recursive computation
- `PaymentTerm` with installment schedules + early discount
- `Payment` decoupled from Invoice, delegates to Move
- `PartialReconcile`/`FullReconcile` for matching debit/credit lines
- `FiscalPosition` for tax/account mapping
- Multi-currency support
- Audit trail with SHA256 hash chain
- Financial reports (Trial Balance, GL, P&L, Balance Sheet)
