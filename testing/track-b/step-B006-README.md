# STEP B006 Testing README — Remaining Pages (Dashboard, Map, Goals, Marketplace, Purchases, Admin)

This directory contains test assets for verifying **STEP B006: Analytics Dashboard, Interactive GIS Map, Reduction Goals, Carbon Marketplace, Offset Purchases, and 3-Tab Admin Console**.

## Prerequisites

* **Node.js**: Version 22 LTS or newer installed.
* **Mock Backend Server**: Run the mock backend server locally on port `8080` (`mocks/mock-backend`).
* **Frontend Dev Server**: Run `npm run dev` in `frontend/` on port `5173`.

---

## Mocking / Substitution Strategy

* **Mock Backend Server (`mocks/mock-backend` on port `:8080`)**:
  - Implements all Section 8 endpoints (`/api/analytics/dashboard`, `/api/shipments/map`, `/api/goals`, `/api/marketplace/listings`, `/api/purchases`, `/api/admin/users`, `/api/admin/emission-factors`, `/api/admin/marketplace/...`).
  - **Dashboard Canned Data**: Serves the Section 8.9 dashboard summary example verbatim.
  - **GIS Map Canned Data**: `/api/shipments/map` serves 3–4 canned points with origin/destination lat/lng coordinates for Leaflet mapping.
  - **Credit Listings Set**: Serves the Section 8.6 carbon credit listing set with persistent demo banner state.
  - **Admin Security Rules**: Enforces `ROLE_ADMIN` check on `/api/admin/*` endpoints and rejects deactivation of `admin@carbontrace.dev`.

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
   ./testing/track-b/step-B006-tests.sh
   ```

3. **Verify Dev Server & Browser UI Flows**:
   Navigate to `frontend/` and boot the dev server:
   ```bash
   cd frontend
   npm run dev
   ```
   Open `http://localhost:5173/` in Google Chrome / Firefox.

---

## What "Pass" Looks Like

### 1. Test Script Output (`step-B006-tests.sh`)
Running `./testing/track-b/step-B006-tests.sh` must output:

```text
=========================================
Verifying STEP B006: Remaining Pages & Feature Completeness
=========================================
Running: npm run build...
PASS: npm run build completed successfully (Exit Code 0).
-----------------------------------------
Executing Endpoint Verification against Mock Backend (http://localhost:8080)
-----------------------------------------
PASS: 1.1 Dashboard Analytics Summary (Happy Path) (HTTP 200)
PASS: 1.2 GIS Map Calculated Routes (Happy Path) (HTTP 200)
PASS: 1.3 Reduction Goals List (Happy Path) (HTTP 200)
PASS: 1.4 Create Reduction Goal (Happy Path) (HTTP 201)
PASS: 1.5 Update Reduction Goal (Happy Path) (HTTP 200)
PASS: 1.6 Marketplace Credit Listings (Happy Path) (HTTP 200)
PASS: 1.7 Offset Purchase History (Happy Path) (HTTP 200)
PASS: 1.8 User Accounts List (Admin Happy Path) (HTTP 200)
PASS: 1.9 Toggle User Account Status (Admin Happy Path) (HTTP 200)
PASS: 1.10 GLEC Emission Factors (Admin Happy Path) (HTTP 200)
PASS: 1.11 Marketplace Sellers List (Admin Happy Path) (HTTP 200)
PASS: 1.12 Restock Credit Listing (Admin Happy Path) (HTTP 200)
PASS: 2.1 Auditor Accessing Admin Users Endpoint (Expected 403) (HTTP 403)
PASS: 2.2 Admin Deactivating Own Account (Expected 400) (HTTP 400)
PASS: 3.1 Dashboard Missing Token (Expected 401) (HTTP 401)
-----------------------------------------
Browser UI Verification Guide
-----------------------------------------
1. Start frontend: npm run dev
2. Navigate to http://localhost:5173/ (Dashboard)
   - Verify stat cards, Recharts monthly trend bar chart, transport mode pie chart, and vendor breakdown.
3. Navigate to http://localhost:5173/emissions-map
   - Verify OpenStreetMap tiles load, Leaflet default icons render without Vite asset errors, origin/destination markers appear, and polyline color scales with emissions.
4. Navigate to http://localhost:5173/goals
   - View goals list, click '+ Create Goal' modal -> save new goal.
5. Navigate to http://localhost:5173/marketplace
   - Verify persistent banner 'Simulated marketplace — demo data', filter by project type & max price.
6. Navigate to http://localhost:5173/purchases
   - Click purchase row to expand and view autonomous AI agent decision reasoning log.
7. Log in as auditor@acme.com -> deep-link to http://localhost:5173/admin
   - AdminRoute guard redirects user away from admin console.
8. Log in as admin@carbontrace.dev -> navigate to http://localhost:5173/admin
   - Verify 3 tabs: User Permissions (role toggle), Emission Factors, Marketplace Inventory (restock).
=========================================
All validations for STEP B006 PASSED.
=========================================
```

### 2. Mock Backend Server Logs
While tests and browser UI flows run, `mocks/mock-backend` logs:

```text
[MOCK BACKEND] GET /api/analytics/dashboard
[MOCK BACKEND] GET /api/shipments/map
[MOCK BACKEND] GET /api/goals
[MOCK BACKEND] POST /api/goals
[MOCK BACKEND] PUT /api/goals/1
[MOCK BACKEND] GET /api/marketplace/listings
[MOCK BACKEND] GET /api/purchases
[MOCK BACKEND] GET /api/admin/users
[MOCK BACKEND] PUT /api/admin/users/2/toggle-active
[MOCK BACKEND] GET /api/admin/emission-factors
[MOCK BACKEND] GET /api/admin/marketplace/sellers
[MOCK BACKEND] PUT /api/admin/marketplace/listings/1/restock
[MOCK BACKEND] GET /api/admin/users
[MOCK BACKEND] PUT /api/admin/users/1/toggle-active
[MOCK BACKEND] GET /api/analytics/dashboard
```
