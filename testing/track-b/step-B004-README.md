# STEP B004 Testing README — Auth Pages

This directory contains test assets for verifying **STEP B004: Auth Pages & Components** (Login, Register, Verify OTP, Forgot Password).

## Prerequisites

* **Node.js**: Version 22 LTS or newer installed.
* **Mock Backend Server**: Run the mock backend server locally on port `8080` (`mocks/mock-backend`).
* **Frontend Dev Server**: Run `npm run dev` in `frontend/` on port `5173`.

---

## Mocking / Substitution Strategy

* **Mock Backend Server (`mocks/mock-backend` on port `:8080`)**:
  - Implements all Section 8.1 auth endpoints (`/register`, `/verify-otp`, `/resend-otp`, `/login`, `/forgot-password`, `/reset-password`).
  - Fixed OTP Code: `482913` (all other codes return `400 Invalid OTP code or expired`).
  - Unverified Email Test: `unverified@acme.com` triggers `400 Email not verified`.
  - Admin Role Test: `admin@carbontrace.dev` logs in with `ROLE_ADMIN` and `jwt-admin-token-...`.
* **External Systems (S3, Gemini, SMTP, FastAPI)**: Simulated by `mocks/mock-backend`.
* **Frontend Verification**: Production code never imports from `mocks/`. The environment variable `VITE_API_BASE_URL` in `.env` dictates the API endpoint target.

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
   ./testing/track-b/step-B004-tests.sh
   ```

3. **Verify Dev Server & Browser UI Flows**:
   Navigate to `frontend/` and boot the dev server:
   ```bash
   cd frontend
   npm run dev
   ```
   Open `http://localhost:5173/login` in Google Chrome / Firefox.

---

## What "Pass" Looks Like

### 1. Test Script Output (`step-B004-tests.sh`)
Running `./testing/track-b/step-B004-tests.sh` must output:

```text
=========================================
Verifying STEP B004: Auth Pages
=========================================
Running: npm run build...
PASS: npm run build completed successfully (Exit Code 0).
-----------------------------------------
Executing Endpoint & Form Flow Verification against Mock Backend (http://localhost:8080)
-----------------------------------------
PASS: 1.1 Register User (Happy Path) (HTTP 201)
PASS: 1.2 Verify OTP (Happy Path & Auto-Login) (HTTP 200)
PASS: 1.3 Resend OTP (Happy Path) (HTTP 200)
PASS: 1.4 Login Auditor (Happy Path) (HTTP 200)
PASS: 1.5 Login Admin (Happy Path) (HTTP 200)
PASS: 1.6 Forgot Password Step 1 (Happy Path) (HTTP 200)
PASS: 1.7 Reset Password Step 2 (Happy Path) (HTTP 200)
PASS: 2.1 Verify OTP Wrong Code (Expected 400) (HTTP 400)
PASS: 2.2 Login Unverified Email (Expected 400) (HTTP 400)
PASS: 2.3 Login Invalid Credentials (Expected 401) (HTTP 401)
-----------------------------------------
Browser UI Verification Guide
-----------------------------------------
1. Start frontend: npm run dev
2. Navigate to http://localhost:5173/register
3. Complete registration form -> redirected to /verify-otp with prefilled email
4. Enter fixed mock OTP '482913' -> auto-login executes and redirects to /
5. Navigate to http://localhost:5173/login
6. Test login with 'unverified@acme.com' -> ErrorMessage displays 'Email not verified'
7. Test login with 'admin@carbontrace.dev' -> logs in with ROLE_ADMIN role
=========================================
All validations for STEP B004 PASSED.
=========================================
```

### 2. Mock Backend Server Logs
While tests and browser UI flows run, `mocks/mock-backend` logs:

```text
[MOCK BACKEND] POST /api/auth/register
[MOCK BACKEND] POST /api/auth/verify-otp
[MOCK BACKEND] POST /api/auth/resend-otp
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/forgot-password
[MOCK BACKEND] POST /api/auth/reset-password
[MOCK BACKEND] POST /api/auth/verify-otp
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/login
```
