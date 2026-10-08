# My Duck Store

Java 17 · Spring Boot 4.1.1 · MongoDB · Maven. Backend only (warehouse UI deferred, store has no UI by design).

## Run on a clean machine
Prerequisites: JDK 17, Maven 3.9+, Docker.

```bash
docker compose up -d mongo          # database on localhost:27017
mvn test                            # unit tests (no DB needed)
mvn spring-boot:run                 # API on http://localhost:8080
```
Override the DB with `MONGODB_URI=mongodb://host:27017/duckstore`.

Concurrency test against the live DB:
```bash
MONGO_IT=true mvn test -Dtest=DuckMergeConcurrencyTest
```

## Architecture
```
shared/      enums (Color, Size), EnumParser, exceptions, one ApiExceptionHandler (RFC 7807)
warehouse/   api (controller, DTOs) -> service -> repo (reads: Spring Data, writes: atomic Mongo ops)
             spi/WarehouseLookup  <- the ONLY thing the store may call
store/       api -> service/OrderService (orchestration)
             packaging/  Strategy per shipping mode + size->package table
             pricing/    PricingCalculator (interface) -> Chain of Responsibility, one handler per stage
```
Patterns: **Strategy** (`ProtectionStrategy`), **Chain of Responsibility** (pricing), **ports & adapters** (`WarehouseLookup`, `PricingCalculator` interfaces).

### Pricing chain
Pricing works on the **whole order**. `OrderService` resolves each item's unit price and packaging, then calls the `PricingCalculator` interface once. `DefaultPricingCalculator` wires the chain; the link order is the stage order:

| # | Handler | Level | Does |
|---|---|---|---|
| 1 | `BasePriceHandler` | line, then order | line cost = quantity x unit price; order goods total = sum of lines |
| 2 | `VolumeDiscountHandler` | order | total units > 100: -20% of the goods total |
| 3 | `PackageCostHandler` | line | wood +5%, plastic +10%, cardboard -1% of that line's cost after the discount (each line has its own package) |
| 4 | `DestinationChargeHandler` | order | USA 18%, Bolivia 13%, India 19%, other 15% of the order base |
| 5 | `ShippingChargeHandler` | order | sea +400 once; land +10 per unit; air +30 per unit, -15% of the air charge above 1000 total units. Last link: the chain stops here |

Each handler does its own stage and passes a per-request `PricingState` on. To add a stage, write a handler and add one `linkWith(...)` line.

## Warehouse API
| Op | Request | Notes |
|---|---|---|
| Add | `POST /api/ducks` `{"color":"Red","size":"Large","price":5.00,"quantity":10}` | 201 created, 200 merged into existing |
| List | `GET /api/ducks?page=0&size=20&sortBy=quantity&direction=asc` | paginated, deleted excluded |
| Edit | `PATCH /api/ducks/{id}` `{"price":6.5,"quantity":40}` | either field optional; color/size not accepted |
| Delete | `DELETE /api/ducks/{id}` | 204, logical (`deleted=true`) |

## Store API
`POST /api/store/orders`
```json
{ "items": [ {"color":"Red","size":"Large","quantity":10},
             {"color":"Green","size":"Small","quantity":200} ],
  "country": "USA", "shippingMode": "Air" }
```
Response: per line `packageType`, `protections`, `unitPrice`, `goodsCost`, package-cost `adjustments` and line `total`; plus order-level `totalUnits`, `goodsCost`, itemized `adjustments` (bulk discount, destination, shipping) and `totalToPay` (USD). `totalToPay` = sum of line totals + order adjustments.
Errors: 400 invalid input/validation, 404 no such duck, 409 insufficient stock.

## Decisions on ambiguity
1. **Order carries a list of items** (color/size/quantity each) with one country and one shipping mode. Real orders mix ducks; a single-item order is a list of one.
2. **An order is one shipment** (one destination, one shipping mode). **"Units" means the sum of quantities across all items.** The bulk discount (>100), the air bulk reduction (>1000) and the sea flat fee are evaluated **once per order**, so splitting an order into several items cannot dodge them. Packaging stays **per item** (a Red Small and a Yellow XLarge get different packages), and the package cost is computed per item.
3. **Price resolution:** several records may share color+size at different prices. We use the **cheapest live record that alone holds the requested quantity**. If ducks of that color/size exist but none has enough stock -> 409 (never silently mix price tiers). No such duck -> 404. The store is a **quote**: it does not decrement stock.
4. **Percentage base:** "% of the total cost" = cost after the bulk discount. Destination % uses the order's discounted total; package % uses each line's cost after the same discount rate. Percentages never compound on each other. Shipping charges are flat/per-unit and not part of the base.
5. **Thresholds are strict and order-wide:** "more than 100" -> 101+ total units; "exceeds 1000" -> 1001+ total units. The air reduction is 15% of the air charge only.
6. **Rounding:** `BigDecimal`, each adjustment rounded HALF_UP to cents, so the breakdown sums exactly to the total (line totals + order adjustments = `totalToPay`).
7. **Destination** is case-insensitive; `USA/US/United States` are the USA; any unrecognised country is "any other" (15%).
8. **Merge invariant under concurrency** is enforced by the database, not by check-then-insert: a *partial unique index* on `(color, size, price)` where `deleted=false`, plus one atomic upsert with `$inc`. A race either increments the same document or hits the index and retries as an increment.
9. **Deleted ducks don't block re-adding** the same variant (index ignores deleted rows); a new live record is created.
10. **Edit** can't merge: changing a price so it collides with another live duck returns 409. Quantity may be set to 0 (stock-out); add requires > 0.
11. **Prices** are `Double` per the spec entity, limited to 2 decimals, converted to `BigDecimal` for all math.
12. **Ids** are Integers from an atomic counter collection; gaps can occur after a lost race.
13. **Default list sort** is quantity ascending (lowest stock first); `id` is a tie-breaker so paging is stable. `sortBy` is whitelisted.
14. **Frontend** deferred as instructed; the paginated API carries what the table needs.
