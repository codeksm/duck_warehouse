# My Duck Store

A small warehouse application and an order-pricing API for an online shop that sells rubber ducks.

- **Stack:** Java 17, Spring Boot 4.1.1, MongoDB, Maven
- **Scope:** backend only. The warehouse UI is deferred; the store has no UI by design.
- **Modules:** `warehouse` (CRUD with a concurrency-safe merge rule) and `store` (order quote: packaging + pricing)

---

## 1. Run it

Pick **one** option. Option 1 needs only Docker.

### Option 1: Docker Compose (recommended, runs API + MongoDB)

**Prerequisites**
- Docker Desktop (or Docker Engine) with Compose v2. Check: `docker compose version`
- Free ports **8080** (API) and **27017** (MongoDB)
- No JDK or Maven needed: the jar is built inside Docker, so there is no pre-built jar to worry about
- Internet access on the first build (pulls base images and Maven dependencies)

**Steps**
```bash
git clone <repo-url>
cd <repo-folder>
docker compose up --build -d
docker compose ps
```
- First build takes a few minutes; later runs reuse cached layers.
- `docker compose ps` should show `mongo` as `healthy` and `app` as `running`.
- Confirm startup: `docker compose logs app` and look for a line containing `Started`.
- The API is now at **http://localhost:8080**. Jump to [Try the API](#2-try-the-api).

**Day-to-day commands**
```bash
docker compose logs -f app      # follow API logs
docker compose down             # stop; data is kept in the mongo-data volume
docker compose down -v          # stop AND delete the database data (clean slate)
docker compose up --build -d    # rebuild after code changes
```

**How it works:** a multi-stage `Dockerfile` builds the jar with Maven on JDK 17 and ships it in a small JRE image running as a non-root user. Compose starts `mongo` first, waits for its healthcheck, then starts `app` with `MONGODB_URI=mongodb://mongo:27017/duckstore` (inside the compose network the database host is the service name `mongo`, not `localhost`). `target/` is excluded by `.gitignore` and `.dockerignore`, so a stale local jar can never be picked up.

### Option 2: Local development (JDK + Maven, MongoDB in Docker)

**Prerequisites:** JDK 17, Maven 3.9+, Docker (for MongoDB only), free ports 8080 and 27017.

```bash
git clone <repo-url>
cd <repo-folder>

docker compose up -d mongo      # starts only the database on localhost:27017
mvn test                        # unit tests (no database needed)
mvn spring-boot:run             # API on http://localhost:8080
```
- To use a different database: set `MONGODB_URI`, e.g. `MONGODB_URI=mongodb://host:27017/duckstore`.
- Do not run Option 1 and Option 2 at the same time: both use port 8080.
- Stop MongoDB with `docker compose down` (add `-v` to delete the data).

### Running the tests
```bash
mvn test                                                    # unit tests: pricing, packaging
MONGO_IT=true mvn test -Dtest=DuckMergeConcurrencyTest      # concurrency test, needs MongoDB running
```
The concurrency test fires 32 parallel adds of the same duck and asserts there is exactly one record with the summed quantity. It is skipped unless `MONGO_IT=true` is set.

---

## 2. Try the API

All examples use `http://localhost:8080`. Pick the block for your shell.

### macOS / Linux / Git Bash / WSL
```bash
# Add a duck -> 201 Created
curl -i -X POST http://localhost:8080/api/ducks -H 'Content-Type: application/json' \
  -d '{"color":"Red","size":"Large","price":5.00,"quantity":10}'

# Add the SAME duck again -> 200 OK, quantity becomes 15 (merged, no duplicate)
curl -i -X POST http://localhost:8080/api/ducks -H 'Content-Type: application/json' \
  -d '{"color":"Red","size":"Large","price":5.00,"quantity":5}'

# List (paginated, sorted by quantity)
curl "http://localhost:8080/api/ducks?page=0&size=20&sortBy=quantity&direction=asc"

# List including logically deleted ducks (optional, default false)
curl "http://localhost:8080/api/ducks?showDeleted=true"

# Edit price and/or quantity (replace 1 with a real id)
curl -X PATCH http://localhost:8080/api/ducks/1 -H 'Content-Type: application/json' \
  -d '{"price":6.50,"quantity":40}'

# Delete (logical) -> 204
curl -i -X DELETE http://localhost:8080/api/ducks/1

# Order quote
curl -X POST http://localhost:8080/api/store/orders -H 'Content-Type: application/json' \
  -d '{"items":[{"color":"Red","size":"Large","quantity":5}],"country":"USA","shippingMode":"Land"}'
```

### Windows `cmd`
`cmd` does not understand single quotes or `\` line continuation. Use double quotes, escape the quotes inside the JSON with `\"`, and keep each command on **one line**.
```bat
curl -i -X POST http://localhost:8080/api/ducks -H "Content-Type: application/json" -d "{\"color\":\"Red\",\"size\":\"Large\",\"price\":5.00,\"quantity\":10}"

curl "http://localhost:8080/api/ducks?page=0&size=20&sortBy=quantity&direction=asc"

curl "http://localhost:8080/api/ducks?showDeleted=true"

curl -X PATCH http://localhost:8080/api/ducks/1 -H "Content-Type: application/json" -d "{\"price\":6.50,\"quantity\":40}"

curl -i -X DELETE http://localhost:8080/api/ducks/1

curl -X POST http://localhost:8080/api/store/orders -H "Content-Type: application/json" -d "{\"items\":[{\"color\":\"Red\",\"size\":\"Large\",\"quantity\":5}],\"country\":\"USA\",\"shippingMode\":\"Land\"}"
```

### Any shell (no quoting problems): JSON from a file
Save the body as `duck.json`:
```json
{"color":"Red","size":"Large","price":5.00,"quantity":10}
```
then:
```bash
curl -i -X POST http://localhost:8080/api/ducks -H "Content-Type: application/json" -d @duck.json
```
In **PowerShell** type `curl.exe` instead of `curl` (plain `curl` is an alias for a different command). Postman or any REST client works too.

> **Troubleshooting a 415 "Unsupported Media Type":** the `Content-Type: application/json` header did not reach the server, almost always because of shell quoting (see the `cmd` block above or use `-d @file.json`).

---

## 3. API reference

### Warehouse
| Operation | Request | Result |
|---|---|---|
| Add | `POST /api/ducks` `{"color","size","price","quantity"}` | `201` new record, or `200` merged into the existing one |
| List | `GET /api/ducks?page=0&size=20&sortBy=quantity&direction=asc[&showDeleted=true]` | Paginated; deleted ducks are hidden unless `showDeleted=true` |
| Edit | `PATCH /api/ducks/{id}` `{"price"?, "quantity"?}` | Only price and quantity; color and size are not accepted. At least one field required |
| Delete | `DELETE /api/ducks/{id}` | `204`; logical delete (`deleted=true`), record stays in the database |

- `color`: Red, Green, Yellow, Black. `size`: XLarge, Large, Medium, Small, XSmall. Both case-insensitive.
- List params: `page` (>= 0), `size` (1..100, default 20), `sortBy` one of `id, color, size, price, quantity` (default `quantity`), `direction` `asc|desc` (default `asc`), `showDeleted` `true|false` (optional, default `false`: logically deleted ducks are included only when `true`). `id` is a tie-breaker so paging is stable.
- List response: `{ "content": [...], "page", "size", "totalElements", "totalPages" }`. Each duck has `id, color, size, price, quantity, deleted`; `deleted` is `true` only for records returned because of `showDeleted=true`.

### Store: order quote
`POST /api/store/orders`
```json
{
  "items": [
    { "color": "Red",    "size": "Small",  "quantity": 60 },
    { "color": "Yellow", "size": "XLarge", "quantity": 50 }
  ],
  "country": "USA",
  "shippingMode": "Air"
}
```
Response (shape):
- `destination`, `shippingMode`, `currency` (USD), `totalUnits`
- `lines[]`: per item `color, size, quantity, unitPrice, packageType, protections[], goodsCost, adjustments[], total`. The line `total` excludes order-level charges.
- `goodsCost`, `adjustments[]` (order level: bulk discount, destination, shipping), `totalToPay`
- `totalToPay` = sum of line totals + order adjustments. Every adjustment is a signed amount (negative = discount).

### Errors (RFC 7807 problem JSON)
| Status | When |
|---|---|
| 400 | Validation failed, unknown color/size/shipping mode, malformed JSON, bad paging params, non-boolean `showDeleted` |
| 404 | Duck id not found (or already deleted); no duck of that color/size for an order |
| 409 | Not enough stock for an order line; edit would collide with another live duck |

---

## 4. Architecture

```
shared/      Color, Size, EnumParser, exceptions, one ApiExceptionHandler
warehouse/   api (controller, DTOs) -> service -> repo (reads: Spring Data, writes: atomic Mongo ops)
             spi/WarehouseLookup   <- the ONLY thing the store may call
store/       api -> service/OrderService (orchestration)
             packaging/   Strategy per shipping mode + size -> package table
             pricing/     PricingCalculator (interface) -> Chain of Responsibility, one handler per stage
```

**Patterns:** Strategy (protection per shipping mode), Chain of Responsibility (pricing stages), ports and adapters (`WarehouseLookup`, `PricingCalculator` interfaces). The store never touches warehouse internals, and `OrderService` depends only on interfaces.

### Packaging
Per item: size picks the package (XLarge/Large = wood, Medium = cardboard, Small/XSmall = plastic). The shipping mode decides the protection: air = polystyrene balls for wood/cardboard and bubble-wrap bags for plastic; land = polystyrene balls; sea = moisture-absorbing beads + bubble-wrap bags. A new shipping mode is a new strategy class.

### Pricing chain
Pricing works on the **whole order**. `DefaultPricingCalculator` wires the chain and the link order is the stage order:

| # | Handler | Level | Does |
|---|---|---|---|
| 1 | `BasePriceHandler` | line, then order | line cost = quantity x unit price; order goods total = sum of lines |
| 2 | `VolumeDiscountHandler` | order | total units > 100: -20% of the goods total |
| 3 | `PackageCostHandler` | line | wood +5%, plastic +10%, cardboard -1% of that line's cost after the discount |
| 4 | `DestinationChargeHandler` | order | USA 18%, Bolivia 13%, India 19%, other 15% of the order base |
| 5 | `ShippingChargeHandler` | order | sea +400 once; land +10 per unit; air +30 per unit, -15% of the air charge above 1000 total units. Last link: the chain stops here |

Each handler does its own stage and passes a per-request `PricingState` on. To add a stage, write a handler and add one `linkWith(...)` line.

**Worked example** (Red Small x60 at 10.00 plus Yellow XLarge x50 at 20.00, USA, Air): goods 1600.00 for 110 units; bulk discount -320.00; plastic +48.00 and wood +40.00 on the discounted line costs; USA 18% of 1280 = +230.40; air 110 x 30 = +3300.00; **total 4898.40**.

---

## 5. Decisions on ambiguity

1. **Orders carry a list of items** (color, size, quantity each) with one destination and one shipping mode. Real orders mix ducks; a single-item order is a list of one.
2. **An order is one shipment, and "units" means the sum of quantities across all items.** The bulk discount (>100), the air bulk reduction (>1000) and the sea flat fee are evaluated **once per order**, so splitting an order into several items cannot dodge them. **Packaging stays per item** (a Red Small and a Yellow XLarge get different packages) and package cost is computed per item.
3. **Price resolution.** Several records may share color + size at different prices. The store uses the **cheapest live record that alone holds the requested quantity**. If ducks of that color/size exist but none has enough stock, the API answers 409 (price tiers are never silently mixed). No such duck: 404. The store only **quotes**: it does not reserve or decrement stock. Two lines for the same color/size are each checked against stock on their own.
4. **Percentage base.** "% of the total cost" means the cost after the bulk discount. Destination % uses the order's discounted total; package % uses each line's cost after the same discount rate. Percentages never compound on each other. Shipping charges are flat or per-unit and are not part of the base.
5. **Thresholds are strict and order-wide:** "more than 100" means 101 or more total units; "exceeds 1000" means 1001 or more. The air reduction applies to the air charge only.
6. **Rounding:** `BigDecimal`, each adjustment rounded HALF_UP to cents, so the breakdown sums exactly to the total.
7. **Destination** matching is case/whitespace-insensitive; `USA`, `US` and `United States` are the USA; any unrecognised country is "any other" (15%).
8. **The merge rule holds under concurrency because the database enforces it.** A partial unique index on `(color, size, price)` where `deleted=false`, plus atomic single-statement writes (`$inc`, upsert). Two simultaneous adds of the same duck either increment the same document, or one inserts and the other hits the unique index and retries as an increment. A `synchronized` block would not work across several app instances.
9. **Deleted ducks do not block re-adding** the same variant (the index ignores deleted rows); a fresh live record is created.
10. **Edit cannot merge.** Changing a price so it collides with another live duck returns 409 (edit that duck instead). Quantity may be set to 0 (stock-out); an add requires quantity > 0.
11. **Prices** are `Double` per the spec entity, limited to 2 decimals, and converted to `BigDecimal` for all calculations.
12. **Ids** are Integers from an atomic counter collection. They are unique and increasing but **not guaranteed gap-free** (for example after a lost insert race). Integer range is ample for the number of distinct duck records; moving to `Long` would be the change if that ever stopped being true.
13. **Default list sort** is quantity ascending (lowest stock first). `sortBy` is whitelisted.
14. **Quantity limits** are input sanity checks: a single add is capped at 1,000,000, an edit at 1,000,000, and an order line at 10,000,000. **Known limitation:** stock that grows through many repeated adds is not guarded against exceeding the Integer maximum (2,147,483,647). The fix would be a conditional atomic update that rejects an overflowing add with 409.
15. **Deleted ducks are hidden by default.** The spec says deleted ducks never appear in the listing, so `showDeleted` defaults to `false` and the default listing is unchanged. `showDeleted=true` is an opt-in view (for support or audit) that includes logically deleted records, each flagged `deleted: true`. Deleted records are never returned by the order/price lookup, regardless of this flag.