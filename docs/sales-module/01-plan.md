# Sales Module Overhaul — Implementation Plan

## Objective

Transform the current basic sales CRUD into a full Odoo-inspired sales management system: quotations → sale orders, price lists, sales teams, multi-currency, tax integration with the finance module, discounts, shipping, returns/credit notes, and a unified Partner (res.partner) model.

---

## Architecture Pattern

```
Controller (REST) → Service (Business Logic) → Entity (JPA) → Repository (Spring Data)
                                             ↕
                                     DTO (Request/Response)
```

### Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Partner model | **Unified** — single `Partner` entity for customers, contacts, addresses | Odoo's `res.partner` pattern; simpler than separate Customer table |
| Quotation vs Order | **Separate lifecycle** — Quotation (`DRAFT`/`SENT`) → Sale Order (`CONFIRMED`/`SHIPPED`) | Odoo's pattern; distinct stages with different actions |
| Price Lists | **Rule-based** — `PriceList` + `PriceListItem` with min_qty, date windows, discount % | Flexible pricing without hard-coding prices per order line |
| Tax integration | **Link to finance `Tax` entity** — M2M on order lines | Single tax definition shared across modules |
| Discount | **Line-level % discount** — computed on confirmation | Odoo's standard approach |
| Order number | **Database sequence** — replace `count()+1` race-condition fix | Atomic, gapless sequence |

---

## New Entities

### SalesTeam

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| name | String | NOT NULL |
| description | String | TEXT, nullable |
| memberIds | List<Long> | User IDs (JSONB or join table) |
| targetRevenue | BigDecimal | Monthly/quarterly target |
| isActive | Boolean | default true |
| createdAt | LocalDateTime | auto |
| updatedAt | LocalDateTime | auto |

**Frontend impact**: New section in Sales module. GET/POST/PUT/DELETE `/api/v1/sales-teams`.

### PriceList + PriceListItem

**PriceList**:

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| name | String | NOT NULL, e.g. "Retail", "Wholesale" |
| currencyId | Long | FK → Currency |
| validFrom | LocalDate | nullable |
| validTo | LocalDate | nullable |
| isActive | Boolean | default true |
| createdAt | LocalDateTime | auto |
| updatedAt | LocalDateTime | auto |

**PriceListItem**:

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| priceListId | Long | FK → PriceList |
| productId | Long | FK → Product |
| minQuantity | BigDecimal | Minimum qty for this price (default 1) |
| fixedPrice | BigDecimal | Override price (if set, overrides base) |
| discountPercent | BigDecimal | Discount % off base price |
| validFrom | LocalDate | nullable, date-based rule |
| validTo | LocalDate | nullable |

**Frontend impact**: New section for price list management. `GET/POST /api/v1/price-lists`, `GET/POST /api/v1/price-lists/{id}/items`.

### Incoterm

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| code | String | e.g. "EXW", "FOB", "CIF" — UNIQUE |
| name | String | e.g. "Ex Works", "Free on Board" |
| description | String | TEXT, nullable |

**Frontend impact**: Selection dropdown on SalesOrder form. `GET /api/v1/incoterms`.

### Partner (unified customer/contact model)

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| name | String | Company or individual name |
| type | Enum: COMPANY, INDIVIDUAL, CONTACT | COMPANY has child CONTACTs |
| parentId | Long | FK → Partner (self-ref, nullable) |
| email | String | nullable |
| phone | String | nullable |
| mobile | String | nullable, separate from phone |
| website | String | nullable |
| taxId | String | VAT/Tax registration number |
| address | String | Full address text |
| city | String | nullable |
| state | String | nullable |
| zipCode | String | nullable |
| country | String | nullable |
| isActive | Boolean | default true |
| creditLimit | BigDecimal | default 0 |
| paymentTermId | Long | FK → PaymentTerm (finance module) |
| pricelistId | Long | FK → PriceList (default pricelist) |
| salespersonId | Long | FK → User (assigned salesperson) |
| teamId | Long | FK → SalesTeam |
| notes | String | TEXT |
| createdAt | LocalDateTime | auto |
| updatedAt | LocalDateTime | auto |

**Frontend impact**: `Customer` gets replaced by `Partner`. The `GET/POST /api/v1/partners` endpoint replaces `/api/v1/customers`. Field `type` determines whether it's a company, individual, or contact.

---

## Enhanced Existing Entities

### SalesOrder (new/changed fields)

| Field | Change | Type | Notes |
|-------|--------|------|-------|
| `status` | **Changed** | `OrderStatus` | Removed `INVOICED`, added `SENT` |
| `paymentTermId` | **New** | Long (FK → PaymentTerm) | Links to finance module `PaymentTerm` |
| `pricelistId` | **New** | Long (FK → PriceList) | Which pricelist was applied |
| `currencyId` | **New** | Long (FK → Currency) | Currency of the order (defaults to company currency) |
| `incotermId` | **New** | Long (FK → Incoterm) | Shipping terms |
| `teamId` | **New** | Long (FK → SalesTeam) | Sales team assignment |
| `salespersonId` | **New** | Long (FK → User) | Assigned salesperson |
| `partnerInvoiceId` | **New** | Long (FK → Partner) | Invoice address |
| `partnerShippingId` | **New** | Long (FK → Partner) | Shipping address |
| `validityDate` | **New** | LocalDate | Quotation expiry date |
| `amountUntaxed` | **New** | BigDecimal | Subtotal before tax |
| `amountTax` | **New** | BigDecimal | Total tax amount |
| `amountDiscount` | **New** | BigDecimal | Total discount amount |
| `allowedCredit` | **Removed** | — | Replaced by customer credit limit on Partner |

**Frontend impact**: SalesOrder form needs new fields: payment term dropdown, pricelist dropdown, currency selector, incoterm dropdown, sales team selector, salesperson selector, invoice/shipping address pickers, validity date picker.

### SalesOrderLine (new/changed fields)

| Field | Change | Type | Notes |
|-------|--------|------|-------|
| `discount` | **New** | BigDecimal | Discount % (0-100) |
| `taxIds` | **New** | List<Long> (M2M → Tax) | Tax IDs applied to this line |
| `priceSubtotal` | **New** | BigDecimal | Line total before tax |
| `priceTotal` | **New** | BigDecimal | Line total after tax |
| `sequence` | **New** | Integer | Line ordering |
| `displayType` | **New** | Enum: PRODUCT, SECTION, NOTE | For section/note lines in order |
| `productUom` | **New** | String | Unit of measure |

**Frontend impact**: Each line now has discount % input, tax selector (multi-select), subtotal/total display. Lines can be reordered. Section/note lines can be inserted.

### OrderStatus (changed)

```java
public enum OrderStatus {
    DRAFT,       // New quotation
    SENT,        // Quotation sent to customer (NEW)
    CONFIRMED,   // Customer accepted → becomes Sale Order
    SHIPPED,     // Fully delivered
    CANCELLED    // Cancelled (from DRAFT, SENT, or CONFIRMED)
}
```

**Removed**: `INVOICED` (invoicing is tracked separately via finance Move)
**Added**: `SENT`

**Frontend impact**: Order lifecycle buttons change:
- Draft → Send Quotation (moves to SENT)
- Sent → Confirm Order (moves to CONFIRMED)
- Confirmed → Create Invoice (new action, POST /{id}/invoice)
- Confirmed → Ship (moves to SHIPPED)

---

## New Services

| Service | Key Methods |
|---------|-------------|
| `PriceListService` | `getPrice(productId, partnerId, qty, date)` — compute price from pricelist rules |
| `SalesTeamService` | CRUD, member management |
| `PartnerService` | CRUD, address management, hierarchy |
| `IncotermService` | CRUD (mostly read-only reference data) |

---

## New Endpoints

| Method | Path | Purpose |
|--------|------|--------|
| GET/POST/PUT/DELETE | `/api/v1/sales-teams` | Sales team management |
| GET/POST/PUT/DELETE | `/api/v1/price-lists` | Price list CRUD |
| GET/POST/PUT/DELETE | `/api/v1/price-lists/{id}/items` | Price list items |
| GET/POST/PUT/DELETE | `/api/v1/partners` | Unified partner endpoint (replaces `/customers`) |
| GET | `/api/v1/incoterms` | List incoterms |
| POST | `/api/v1/sales-orders/{id}/send` | Send quotation |
| POST | `/api/v1/sales-orders/{id}/confirm` | Confirm (accept) quotation |
| POST | `/api/v1/sales-orders/{id}/invoice` | Generate invoice from order |
| POST | `/api/v1/sales-orders/{id}/duplicate` | Clone sales order |

---

## Implementation Order

| Phase | Steps | Description |
|-------|-------|-------------|
| **B: Fixes** | B1-B6 | Fix PaymentTerms, OrderStatus, date filtering, createdByName, ProductClient, optimistic locking |
| **A: Foundation** | A1-A10 | SalesTeam, PriceList, Incoterm, Partner entities; enhance SalesOrder/Line |
| **C: Services** | C1-C5 | PriceListService, SalesTeamService, PartnerService, enhanced SalesOrderService |
| **D: Controllers** | D1-D10 | Wire new endpoints, update existing ones |
| **E: DTOs** | E1-E5 | New request/response DTOs + update existing ones |
| **Migration** | M1 | Flyway V24 migration script |
