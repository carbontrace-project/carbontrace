# STEP B002 Testing README

This directory contains test assets for verifying **STEP B002: Mock Backend Server**.

## Prerequisites

* **Node.js**: Version 22 LTS or newer installed.
* **Services**: The React frontend does not need to be running.
* **Mocks**: Run the mock backend server locally.

## Run Order

1. **Start the Mock Backend Server**:
   Navigate to the mock backend workspace and start it:
   ```bash
   cd mocks/mock-backend
   npm start
   ```

2. **Execute Automated Endpoint Tests**:
   Open a separate shell, navigate to the project root, and execute the test runner script using Git Bash or a bash-compatible shell:
   ```bash
   ./testing/track-b/step-B002-tests.sh
   ```

---

## What "Pass" Looks Like

### 1. Test Script Output
Running `./testing/track-b/step-B002-tests.sh` must output:
```text
=========================================
Verifying STEP B002: Mock Backend Endpoints
=========================================
Test: 1.1 Login Auditor (Happy)... PASS (HTTP 200)
Test: 1.2 Login Admin (Happy)... PASS (HTTP 200)
Test: 1.3 Login Unverified (Expected 400)... PASS (HTTP 400)
Test: 1.4 OTP Verify Incorrect (Expected 400)... PASS (HTTP 400)
Test: 1.5 OTP Verify Correct... PASS (HTTP 200)
Test: 2.1 Protected Route No Token (Expected 401)... PASS (HTTP 401)
Test: 2.2 Protected Route Expired Token (Expected 401)... PASS (HTTP 401)
Test: 2.3 Protected Route Valid Token... PASS (HTTP 200)
Test: 2.4 Admin Route by Auditor (Expected 403)... PASS (HTTP 403)
Test: 2.5 Admin Route by Admin... PASS (HTTP 200)
Test: 3.1 List Vendors... PASS (HTTP 200)
Test: 3.2 Generate Upload URL... PASS (HTTP 200)
Test: 3.3 Create Shipment... PASS (HTTP 201)
Test: 3.4 Review calculated shipment id 7 (Expected 400)... PASS (HTTP 400)
Test: 3.5 Calculate emissions shipment id 502 (Expected 502)... PASS (HTTP 502)
Test: 3.6 Calculate emissions shipment id 1... PASS (HTTP 200)
Test: 3.7 Map Coordinates... PASS (HTTP 200)
Test: 4.1 Get Marketplace Credits (Public)... PASS (HTTP 200)
Test: 4.2 Purchase Offset Low Budget... PASS (HTTP 200)
Test: 4.3 Purchase Offset High Budget... PASS (HTTP 201)
Test: 5.1 Create Goal... PASS (HTTP 201)
Test: 5.2 Get Analytics Dashboard... PASS (HTTP 200)
-----------------------------------------
All validations for STEP B002 PASSED.
=========================================
```

### 2. Mock Backend Server Logs
While tests are running, the mock backend console must log the incoming requests with their respective paths:
```text
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/verify-otp
[MOCK BACKEND] POST /api/auth/verify-otp
[MOCK BACKEND] GET /api/users/me
[MOCK BACKEND] GET /api/users/me
[MOCK BACKEND] GET /api/users/me
[MOCK BACKEND] GET /api/admin/users
[MOCK BACKEND] GET /api/admin/users
[MOCK BACKEND] GET /api/vendors
[MOCK BACKEND] POST /api/shipments/upload-url
[MOCK BACKEND] POST /api/shipments
[MOCK BACKEND] PUT /api/shipments/7/review
[MOCK BACKEND] POST /api/shipments/502/calculate
[MOCK BACKEND] POST /api/shipments/1/calculate
[MOCK BACKEND] GET /api/shipments/map
[MOCK BACKEND] GET /api/marketplace/credits
[MOCK BACKEND] POST /api/purchases
[MOCK BACKEND] POST /api/purchases
[MOCK BACKEND] POST /api/goals
[MOCK BACKEND] GET /api/analytics/dashboard
```

---

## Mocking / Substitution Strategy
* **External Systems (S3, Gemini, SMTP, FastAPI)**: None are integrated or required. This mock backend service stands in for all Spring Boot endpoints locally. Direct uploads to S3 are simulated by requesting upload URLs pointing to a mock PUT route on port `8080`, which responds directly with `200 OK`.
