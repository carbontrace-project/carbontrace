# MOCK — replaced in A-SWAP-1 (/extract), A-SWAP-2 (/calculate), A-SWAP-3 (/agent/purchase) — one route deleted per swap step
"""
CarbonTrace — mocks/mock-ai — stand-in for the FastAPI AI service.

Built by Track A at STEP A020 (COMMANDO.md Section 25.3) so that STEP A021
(CalculationService) and STEP A026 (the purchase commit path) exercise the real
HTTP path — RestTemplate, timeouts, JSON binding, the 502 failure branch —
against real network responses, months before Track C's service exists.

Contract: COMMANDO.md Section 8.11, snake_case, no ApiResponse wrapper (that
wrapper belongs to Spring Boot, Section 8.1-8.10; the FastAPI contracts in 8.11
return bare bodies). Every success body below is the Section 8.11 example
character for character, held as literal JSON text rather than built from Python
objects so that "20430.00" stays "20430.00" and a diff against the spec is
trivially clean.

Section 25.3 tooling rule: "WireMock standalone or a ~50-line Python script; must
NOT require the AI track's codebase." This is the Python option, on the standard
library alone — no pip install, no Docker, no test framework, no import of
ai-service/ or backend/.

Run:  python mocks/mock-ai/mock_ai.py      (listens on 127.0.0.1:8000)
"""

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HOST = "127.0.0.1"  # internal service, localhost only (COMMANDO.md Section 9)
PORT = 8000

# --- Section 8.11 POST /extract — the Shenzhen -> Rotterdam example ----------
EXTRACT = """{
    "invoice_number": "OBF-88213",
    "carrier_name": "OceanBridge Freight",
    "shipment_date": "2026-06-28",
    "origin_city": "Shenzhen",
    "origin_country": "China",
    "origin_lat": 22.5431,
    "origin_lng": 114.0579,
    "destination_city": "Rotterdam",
    "destination_country": "Netherlands",
    "destination_lat": 51.9244,
    "destination_lng": 4.4777,
    "transport_mode": "SEA",
    "fuel_type": "HEAVY_FUEL_OIL",
    "weight_tonnes": 18.5,
    "distance_km": null,
    "field_confidence": {
        "weight_tonnes": "HIGH", "transport_mode": "HIGH",
        "origin_city": "HIGH", "destination_city": "HIGH",
        "fuel_type": "MEDIUM", "shipment_date": "HIGH",
        "origin_lat": "MEDIUM", "origin_lng": "MEDIUM",
        "destination_lat": "MEDIUM", "destination_lng": "MEDIUM"
    },
    "overall_confidence": "MEDIUM"
}"""

# --- Section 8.11 POST /calculate -------------------------------------------
CALCULATE = """{
    "distance_km": 20430.00,
    "distance_source": "COMPUTED",
    "total_emissions_kgco2e": 4157.51,
    "formula": "18.5 t × 20430.00 km × 0.011 kgCO2e/t·km"
}"""

# --- Section 8.11 POST /agent/purchase — success case -----------------------
PURCHASE = """{
    "decision": "PURCHASE",
    "listing_id": 3,
    "tonnes": 4.219,
    "price_per_tonne_usd": 14.50,
    "total_cost_usd": 61.18,
    "transaction_reference": "SIM-9F3A2C71",
    "reasoning": "Compared 8 active listings. Two had sufficient availability within budget; chose the Gold Standard reforestation project at $14.50/t over the VCS renewable project at $13.75/t because ... "
}"""

# --- Section 8.11 POST /agent/purchase — no viable option -------------------
# The example omits tonnes / price / total_cost / transaction_reference; it is
# reproduced as printed, because the example IS the contract (Section 25.2).
NO_PURCHASE = """{
    "decision": "NO_PURCHASE",
    "listing_id": null,
    "reasoning": "No active listing offers 4.219 tonnes within the $20 budget; the cheapest viable option costs $57.98."
}"""

HEALTH = '{"status": "ok"}'

# Failure bodies use FastAPI's own HTTPException shape ({"detail": ...},
# Section 23) — NOT ApiResponse.error, which is Spring Boot's format and would
# be contract drift here.
NOT_FOUND = '{"detail": "Not Found"}'
METHOD_NOT_ALLOWED = '{"detail": "Method Not Allowed"}'
JSON_DECODE_ERROR = '{"detail": "JSON decode error"}'

POST_ROUTES = {"/extract": EXTRACT, "/calculate": CALCULATE, "/agent/purchase": PURCHASE}

# The ONE behavior beyond the Section 8.11 examples, mandated by STEP A020 so
# that Spring Boot's NO_PURCHASE branch is testable: a budget at or below the
# $20 the NO_PURCHASE example itself names returns that example.
BUDGET_FLOOR_USD = 20


class MockAiHandler(BaseHTTPRequestHandler):
    """Serves the four Section 8.11 routes and nothing else."""

    protocol_version = "HTTP/1.1"
    server_version = "mock-ai"

    def do_GET(self):
        if self.path == "/health":
            self._send(200, HEALTH)
        elif self.path in POST_ROUTES:
            self._send(405, METHOD_NOT_ALLOWED)
        else:
            self._send(404, NOT_FOUND)

    def do_POST(self):
        # Read the body FIRST, always: an unread body desynchronises the next
        # request on a keep-alive connection, which RestTemplate uses.
        raw = self.rfile.read(int(self.headers.get("Content-Length") or 0))

        if self.path not in POST_ROUTES:
            self._send(405 if self.path == "/health" else 404,
                       METHOD_NOT_ALLOWED if self.path == "/health" else NOT_FOUND)
            return

        try:
            payload = json.loads(raw or b"{}")
        except ValueError:
            self._send(422, JSON_DECODE_ERROR)
            return
        if not isinstance(payload, dict):
            self._send(422, JSON_DECODE_ERROR)
            return

        if self.path == "/agent/purchase":
            budget = payload.get("max_budget_usd")
            broke = isinstance(budget, (int, float)) and not isinstance(budget, bool) \
                and budget <= BUDGET_FLOOR_USD
            self._send(200, NO_PURCHASE if broke else PURCHASE)
        else:
            self._send(200, POST_ROUTES[self.path])

    def _send(self, status, body):
        data = body.encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_request(self, code="-", size="-"):
        """Method, path and status only.

        The /extract payload carries a presigned S3 GET URL; COMMANDO.md
        Section 13 forbids logging those in full, so no request body is ever
        echoed here — not even in a mock.
        """
        print(f"mock-ai  {self.command} {self.path} -> {getattr(code, 'value', code)}",
              flush=True)


if __name__ == "__main__":
    print(f"mock-ai  MOCK for the FastAPI AI service (COMMANDO.md Section 8.11)")
    print(f"mock-ai  listening on http://{HOST}:{PORT}  (Ctrl+C to stop)", flush=True)
    try:
        ThreadingHTTPServer((HOST, PORT), MockAiHandler).serve_forever()
    except KeyboardInterrupt:
        print("\nmock-ai  stopped", flush=True)
