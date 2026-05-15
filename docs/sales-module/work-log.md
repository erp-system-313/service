# Sales Module Overhaul — Work Log

## Session 1: 2026-05-14 (Phase B Fixes + New Entities)

### Summary
Completed Phase B fixes and began Phase A foundation for the Sales module overhaul.

### Completed
1. **OrderStatus** — Removed `INVOICED`, added `SENT` for proper quotation lifecycle
2. **PaymentTerms** — Deprecated enum; sales now links to finance `PaymentTerm` entity
3. **Quotation lifecycle** — Added `send()` method, updated `confirm()` to accept DRAFT+SENT
4. **Date filtering** — Fixed `findWithFilters` to actually use `dateFrom`/`dateTo`
5. **createdByName** — Uses `User.getFullName()` instead of email
6. **SalesTeam** — New entity with member management, revenue targets, soft-delete
7. **PriceList + PriceListItem** — Rule-based pricing engine with date/qty matching
8. **Incoterm** — Reference data for shipping terms (EXW, FOB, CIF, etc.)
9. **Enhanced SalesOrder** — Added 12 new fields (paymentTerm, pricelist, currency, incoterm, team, addresses, discounts, optimistic locking)
10. **Enhanced SalesOrderLine** — Added 7 new fields (discount %, tax_ids M2M, subtotal/total, sequence, displayType, UoM)
11. **Partner** — New unified partner entity (company/contact hierarchy, addresses, tax ID, salesperson assignment)

### Tests
- **104 tests, 0 failures, BUILD SUCCESS** — after all entity additions
- Pre-existing test suite unchanged; all new entities are backward-compatible (nullable FKs, `@Builder.Default` for new fields)

### Documentation
- `docs/sales-module/01-plan.md` — created with full plan and DTO specs for frontend
- `docs/sales-module/changelog.md` — created with commit log
- `docs/sales-module/work-log.md` — this file

### Next Steps
- Phase A10: Fix order number generation (replace `count()+1` with DB sequence)
- Phase C: Build full services (PriceListService price computation, PartnerService, enhanced SalesOrderService with quote→invoice flow)
- Phase D: Wire new controllers (partner, pricelist, sales-team endpoints)
- Phase E: Build all DTOs for new fields
- Phase M: Create V24 Flyway migration
