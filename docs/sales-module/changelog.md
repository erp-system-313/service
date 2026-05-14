# Sales Module Overhaul — Changelog

## Commits (chronological)

| Commit | Description |
|--------|-------------|
| `1073dd8` | **OrderStatus**: Remove `INVOICED`, add `SENT` — enables quotation lifecycle (DRAFT → SENT → CONFIRMED → SHIPPED) |
| `4e1ff91` | **PaymentTerms**: Deprecate enum — sales orders now link to finance module's `PaymentTerm` entity |
| `8c9cb0b` | **Quotation lifecycle**: Add `send()` method (DRAFT → SENT); update `confirm()` to accept both DRAFT and SENT statuses. New `PUT /{id}/send` endpoint. |
| `8d06c73` | **Date filtering**: `findWithFilters` query now actually uses `dateFrom`/`dateTo` parameters. Fix `createdByName` to use `User.getFullName()` instead of email. |
| `bfe3d6f` | **Documentation**: Add `docs/sales-module/01-plan.md` — full plan with DTO specs for frontend team |
| `012d2af` | **SalesTeam**: New entity + repository + service + controller (`/api/v1/sales-teams`). Supports member management, revenue targets. |
| `ed6a173` | **PriceList + PriceListItem**: New entities with rule-based price computation engine (`findMatchingRules` query by product, qty, date). `GET/POST /api/v1/price-lists`, `GET/POST/DELETE /{id}/items`. |
| `85ac25d` | **Incoterm**: New reference entity (EXW, FOB, CIF, etc.). `GET /api/v1/incoterms`. |
| `d16319a` | **Enhanced SalesOrder/SalesOrderLine**: Add Odoo-inspired fields — `paymentTerm`, `pricelistId`, `currencyId`, `incoterm`, `team`, `salespersonId`, `partnerInvoiceId`, `partnerShippingId`, `validityDate`, `amountUntaxed`, `amountDiscount`, `@Version` optimistic locking on SalesOrder; `discount`, `taxIds` (M2M → finance `Tax`), `priceSubtotal`, `priceTotal`, `sequence`, `displayType`, `productUom` on SalesOrderLine. |
| `00690fc` | **Partner**: New entity (Odoo `res.partner` equivalent) with company/contact hierarchy, tax ID, address details, payment terms link, pricelist assignment. `GET/POST /api/v1/partners`, `GET /{id}/contacts`. |

## Phase Status

| Phase | Sub-phases | Status |
|-------|------------|--------|
| B: Fixes | B1 (PaymentTerms), B2 (OrderStatus), B3 (date filtering), B4 (createdByName) | ✅ Done |
| A: Foundation | A1 (SalesTeam), A2 (PriceList), A3 (SENT status), A4 (Partner), A5 (tax link), A6 (discount), A7 (payment terms), A8 (multi-currency), A9 (Incoterm), A10 (sequence) | ⏳ A10 pending |
| C: Services | C1 (PriceListService), C2 (SalesTeamService) | ⏳ In progress |
| D: Controllers | D1-D10 | ⏳ Not started |
| E: DTOs | E1-E5 | ⏳ Not started |
| M: Migration | V24__sales_overhaul.sql | ⏳ Not started |
