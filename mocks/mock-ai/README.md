**MOCK — replaced in A-SWAP-1 (/extract), A-SWAP-2 (/calculate), A-SWAP-3 (/agent/purchase) — one route deleted per swap step**

# mocks/mock-ai

Stand-in for the **FastAPI AI service** (COMMANDO.md Section 8.11), built by
Track A at **STEP A020** per COMMANDO.md Section 25.3.

Track C owns the real `ai-service/`. Track A must not wait for it: STEP A021
(`CalculationService`) and STEP A026 (the purchase commit path) need something
answering on port 8000 so the production HTTP path — `RestTemplate`, the
Section 18 timeouts, snake_case JSON binding, and the Section 23 **502** branch
when the service is down — is exercised against real network responses from day
one.

## Run

```bash
python mocks/mock-ai/mock_ai.py
```

- **Port 8000**, bound to `127.0.0.1` — the internal-service address Spring Boot
  already points at via `app.fastapi.base-url` (`FASTAPI_URL`, Section 19). The
  backend needs **no configuration change** to use this mock, and none at swap
  time either: only which process listens on 8000 changes.
- Python **standard library only** — no `pip install`, no virtualenv, no Docker,
  no test framework (Section 25.3: "WireMock standalone or a ~50-line Python
  script"). Any Python 3.8+ runs it.
- Stop with `Ctrl+C`.

## Routes

| Method | Path | Response |
|---|---|---|
| POST | `/extract` | The Section 8.11 `/extract` example: Shenzhen → Rotterdam, `SEA`, `weight_tonnes` 18.5, the example `field_confidence` map, `overall_confidence` `MEDIUM` |
| POST | `/calculate` | The Section 8.11 `/calculate` example: `distance_km` 20430.00, `distance_source` `COMPUTED`, `total_emissions_kgco2e` 4157.51, the formula string |
| POST | `/agent/purchase` | The Section 8.11 **PURCHASE** example (`listing_id` 3, `tonnes` 4.219, `price_per_tonne_usd` 14.50, `total_cost_usd` 61.18, `transaction_reference` `SIM-9F3A2C71`) |
| GET | `/health` | `{"status": "ok"}` |

Every success body is the Section 8.11 example **character for character**. They
are stored as literal JSON text, not built from Python objects, so pinned
precision survives (`20430.00` stays `20430.00`, `14.50` stays `14.50`) and a
diff against the spec is exact:

```bash
diff <(sed -n '1181,1186p' COMMANDO.md) \
     <(curl -s -X POST http://localhost:8000/calculate -d '{}'; echo)
```

The mock is **stateless and request-agnostic**: it does not read
`shipment_id`, `weight_tonnes`, coordinates, or `document_url`, and it computes
nothing. STEP A021 asserts that Spring Boot *persists the mock's canned figures*,
not that the arithmetic is right — the arithmetic is `calculator.py`, Track C's
work, verified at A-SWAP-2.

### The one behavior beyond the examples

`POST /agent/purchase` returns the Section 8.11 **NO_PURCHASE** example when
`max_budget_usd` is present and **≤ 20** — the `$20` the NO_PURCHASE example
itself names. STEP A020 mandates this single rule so Spring Boot's NO_PURCHASE
handling (Section 8.7: no purchase row, the agent's explanation surfaced) is
testable. Nothing else is invented.

### Failure responses

| Case | Status | Body |
|---|---|---|
| Malformed / non-object JSON body | 422 | `{"detail": "JSON decode error"}` |
| Unknown path | 404 | `{"detail": "Not Found"}` |
| Wrong method on a known path | 405 | `{"detail": "Method Not Allowed"}` |

**No 401 or 403 exists here, deliberately.** Section 9: "FastAPI endpoints have
no auth (internal service, localhost during development)". A mock that demanded
a token would be contract drift, and would make Spring Boot pass an
Authorization header the real service will reject at A-SWAP-1.

**No `ApiResponse` wrapper either.** That wrapper is Spring Boot's, covering
Sections 8.1–8.10. The Section 8.11 FastAPI contracts are bare snake_case
bodies, so failures use FastAPI's own `HTTPException` shape (`{"detail": ...}`,
Section 23). `mocks/mock-backend` — Track B's mock of Spring Boot — is where the
`ApiResponse.error` shape belongs.

The **most important failure path is not a route at all**: stopping this process
is how STEP A021 tests the Section 23 502 branch ("AI service unavailable —
please try again", shipment stays `REVIEWED`).

## Boundaries (COMMANDO.md Section 25.4)

- Lives under repo-root `mocks/`; labeled `MOCK — replaced in ...` at the top of
  this README and of `mock_ai.py`.
- **Never imported by production code.** It is a standalone script reached only
  over HTTP; nothing under `backend/` references this directory.
- **No dependency on `ai-service/`** or on any track's codebase — that is
  precisely why Track A can build and run it today.

## Retirement

Deleted **route by route**, not all at once — each swap step removes the route
it retires from `mock_ai.py`, and the last one deletes this directory:

| Swap step | Retires | Real path it proves |
|---|---|---|
| **A-SWAP-1** | `POST /extract` | `ExtractionService` → real FastAPI extraction during shipment creation |
| **A-SWAP-2** | `POST /calculate` | End-to-end calculation against the real deterministic engine |
| **A-SWAP-3** | `POST /agent/purchase` | The real LangChain agent behind the purchase commit path — **and deletes `mocks/mock-ai/` entirely, `GET /health` with it** |

A mock outliving its swap step is a defect (Section 25.4).

## Contract changes

If the real service and this mock ever disagree, **neither side is the
arbiter — COMMANDO.md Section 8 is**. Per Section 25.2 a contract change goes
into Section 8 first, is agreed by all three members, and only then reaches code
and mocks. Never the reverse.
