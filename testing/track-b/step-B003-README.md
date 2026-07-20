# STEP B003 Testing README — Auth Plumbing & Common Components

This directory contains test assets for verifying **STEP B003: Auth Plumbing, Axios Interceptors, Route Guards, and Common Components**.

## Prerequisites

* **Node.js**: Version 22 LTS or newer installed.
* **Mock Server**: Run the mock backend server locally on port `8080` (`mocks/mock-backend`).
* **Frontend Dev Server**: Run `npm run dev` in `frontend/` on port `5173`.

---

## Mocking / Substitution Strategy

* **Mock Backend Server (`mocks/mock-backend` on port `:8080`)**: Stands in for all Spring Boot backend REST endpoints locally.
* **External Services (S3, Gemini, SMTP, FastAPI)**: Are simulated by `mocks/mock-backend` for testing.
* **Frontend Verification**: Production code never imports from `mocks/`. The environment variable `VITE_API_BASE_URL` in `.env` determines whether the app communicates with the mock server or real backend.

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
   Open a terminal, navigate to the project root, and execute the test runner script using Git Bash or bash:
   ```bash
   ./testing/track-b/step-B003-tests.sh
   ```

3. **Verify Dev Server & Browser Interceptor Behavior**:
   Navigate to `frontend/` and boot the dev server:
   ```bash
   cd frontend
   npm run dev
   ```
   Open `http://localhost:5173` in Google Chrome / Firefox.

---

## What "Pass" Looks Like

### 1. Test Script Output (`step-B003-tests.sh`)
Running `./testing/track-b/step-B003-tests.sh` must output:

```text
=========================================
Verifying STEP B003: Frontend Foundation
=========================================
Running: npm run build...
PASS: npm run build completed successfully (Exit Code 0).
-----------------------------------------
Executing Endpoint & Interceptor Verification against Mock Backend (http://localhost:8080)
-----------------------------------------
PASS: 1.1 Login Auditor (Happy Path) (HTTP 200)
PASS: 2.1 Token Refresh (Happy Path) (HTTP 200)
PASS: 3.1 Protected Route Missing Token (Expected 401) (HTTP 401)
PASS: 4.1 Protected Route Expired Token (Expected 401) (HTTP 401)
PASS: 5.1 Admin Route by Auditor (Expected 403) (HTTP 403)
PASS: 6.1 Admin Route by Admin (Expected 200) (HTTP 200)
-----------------------------------------
Browser Console Interceptor Testing Guide
-----------------------------------------
To verify the Axios response interceptor in the browser:
1. Start frontend: npm run dev
2. Open http://localhost:5173 in Chrome/Firefox DevTools console
3. Execute the following JavaScript snippet:

   localStorage.setItem('refreshToken', 'refresh-token-sample');
   import('./src/api/axiosConfig.js').then(({ default: apiClient, setAccessToken }) => {
     setAccessToken('expired-token');
     apiClient.get('/api/vendors').then(res => console.log('Interceptor Retry SUCCESS:', res.data));
   });

Expected Console Behavior:
- 1st request GET /api/vendors with expired-token returns 401.
- Interceptor executes POST /api/auth/refresh and retrieves rotated tokens.
- 2nd request GET /api/vendors retries with new access token and succeeds (HTTP 200).
=========================================
All validations for STEP B003 PASSED.
=========================================
```

### 2. Mock Backend Server Logs
While tests and browser console requests run, `mocks/mock-backend` logs:

```text
[MOCK BACKEND] POST /api/auth/login
[MOCK BACKEND] POST /api/auth/refresh
[MOCK BACKEND] GET /api/vendors
[MOCK BACKEND] GET /api/vendors
[MOCK BACKEND] POST /api/auth/refresh
[MOCK BACKEND] GET /api/vendors
[MOCK BACKEND] GET /api/admin/users
[MOCK BACKEND] GET /api/admin/users
```
