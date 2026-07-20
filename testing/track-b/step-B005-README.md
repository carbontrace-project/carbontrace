# STEP B005 Testing README — Core Auditor Working Surface

This directory contains test assets for verifying **STEP B005: Vendors, Upload Flow, Shipments List/Detail, Review, Calculate, and Offset Purchase**.

## Prerequisites

* **Node.js**: Version 22 LTS or newer installed.
* **Mock Backend Server**: Run the mock backend server locally on port `8080` (`mocks/mock-backend`).
* **Frontend Dev Server**: Run `npm run dev` in `frontend/` on port `5173`.

---

## Mocking / Substitution Strategy

* **Mock Backend Server (`mocks/mock-backend` on port `:8080`)**:
  - Implements all Section 8 endpoints (`/api/vendors`, `/api/shipments/upload-url`, `/api/shipments`, `/api/shipments/:id/review`, `/api/shipments/:id/calculate`, `/api/purchases`).
  - **Fake Presigned S3 Route (`/mock-s3/...`)**: Accepts direct `PUT` uploads with `Content-Type: application/pdf` and **NO `Authorization` header** (zero AWS dependency during Phase B1).
  - **502 Bad Gateway Designated Case**: `POST /api/shipments/502/calculate` returns HTTP `502 Bad Gateway` to verify the frontend "AI service unavailable" error banner.
  - **Calculated Review Guard Case**: `PUT /api/shipments/7/review` returns HTTP `400 Bad Request` to verify review blocking on already calculated shipments.
  - **Marketplace Offset Purchase Simulation**: `POST /api/purchases` matches active listing 3 (or returns `NO_PURCHASE` reasoning for budget $\le \$20$).

---

## Run Order

1. **Start the Mock Backend Server**:
   Navigate to `mocks/mock-backend` and start the server:
   ```bash
   cd mocks/mock-backend
   npm start
   ```
   The mock server will listen on `http://localhost:8080`.

2. **Execute Automated Endpoint & Build Tests**:
   Open a terminal, navigate to the project root, and execute the test runner script:
   ```bash
   ./testing/track-b/step-B005-tests.sh
   ```

3. **Verify Dev Server & Browser UI Flows**:
   Navigate to `frontend/` and boot the dev server:
   ```bash
   cd frontend
   npm run dev
   ```
   Open `http://localhost:5173/vendors` in Google Chrome / Firefox.

---

## What "Pass" Looks Like

### 1. Test Script Output (`step-B005-tests.sh`)
Running `./testing/track-b/step-B005-tests.sh` must output:

```text
=========================================
Verifying STEP B005: Core Auditor Surface
=========================================
Running: npm run build...
PASS: npm run build completed successfully (Exit Code 0).
-----------------------------------------
Executing Endpoint & Working Surface Verification against Mock Backend (http://localhost:8080)
-----------------------------------------
PASS: 1.1 List Vendors (Happy Path) (HTTP 200)
PASS: 1.2 Add Vendor (Happy Path) (HTTP 201)
PASS: 1.3 Edit Vendor by Admin (Happy Path) (HTTP 200)
PASS: 1.4 Toggle Vendor Active by Admin (Happy Path) (HTTP 200)
PASS: 1.5 Request Upload URL (Happy Path) (HTTP 200)
PASS: 1.6 Bare S3 Presigned PUT (NO Auth Header) (HTTP 200)
PASS: 1.7 Create Shipment (Happy Path) (HTTP 201)
PASS: 1.8 List Shipments (Happy Path) (HTTP 200)
PASS: 1.9 Get Shipment Detail (Happy Path) (HTTP 200)
PASS: 1.10 Get Document Presigned GET URL (Happy Path) (HTTP 200)
PASS: 1.11 Save Extraction Review (Happy Path) (HTTP 200)
PASS: 1.12 Calculate Emissions (Happy Path) (HTTP 200)
PASS: 1.13 Purchase Carbon Offset (Happy Path) (HTTP 200)
PASS: 2.1 Calculate Emissions AI Unavailable (Expected 502) (HTTP 502)
PASS: 2.2 Review CALCULATED Shipment (Expected 400) (HTTP 400)
PASS: 3.1 Toggle Active Vendor by Auditor (Expected 403) (HTTP 403)
PASS: 3.2 Shipments List Missing Token (Expected 401) (HTTP 401)
-----------------------------------------
Browser UI Verification Guide
-----------------------------------------
1. Start frontend: npm run dev
2. Navigate to http://localhost:5173/vendors
   - Search, paginate, and verify Add Vendor modal.
   - Verify Edit & Toggle Active controls render ONLY when logged in as admin@carbontrace.dev.
3. Navigate to http://localhost:5173/upload
   - Test dropping a non-PDF file -> client error message displayed.
   - Test dropping a file > 10MB -> client size error displayed.
   - Drop valid PDF invoice -> presigned S3 upload executes with progress bar -> navigates to detail page.
4. Navigate to http://localhost:5173/shipments
   - Filter by status and vendor -> click shipment card to view detail.
5. Navigate to http://localhost:5173/shipments/502
   - Save review -> click Calculate Emissions -> verifies 502 'AI service unavailable' banner is displayed.
6. Navigate to http://localhost:5173/shipments/7
   - Click 'Offset this shipment' -> PurchaseResultModal opens displaying agent reasoning and 'SIMULATED PURCHASE' badge.
=========================================
All validations for STEP B005 PASSED.
=========================================
```

### 2. Mock Backend Server Logs
While tests and browser UI flows run, `mocks/mock-backend` logs:

```text
[MOCK BACKEND] GET /api/vendors
[MOCK BACKEND] POST /api/vendors
[MOCK BACKEND] PUT /api/vendors/1
[MOCK BACKEND] PUT /api/vendors/1/toggle-active
[MOCK BACKEND] POST /api/shipments/upload-url
[MOCK S3] PUT /mock-s3/invoices/2026/07/uuid-test.pdf
[MOCK BACKEND] POST /api/shipments
[MOCK BACKEND] GET /api/shipments
[MOCK BACKEND] GET /api/shipments/1
[MOCK BACKEND] GET /api/shipments/1/document-url
[MOCK BACKEND] PUT /api/shipments/1/review
[MOCK BACKEND] POST /api/shipments/1/calculate
[MOCK BACKEND] POST /api/purchases
[MOCK BACKEND] POST /api/shipments/502/calculate
[MOCK BACKEND] PUT /api/shipments/7/review
[MOCK BACKEND] PUT /api/vendors/1/toggle-active
[MOCK BACKEND] GET /api/shipments
```
