# STEP B001 Testing README

This directory contains test assets for verifying **STEP B001: Frontend Scaffold**.

## Prerequisites

- **Node.js**: Version 22 LTS installed.
- **External Services / Mocks**: None required for this step. (The mock backend and external API integration will be introduced in subsequent steps).

## Run Order

1. **Build Verification**:
   Execute the test script to verify that the Vite project compiles cleanly for production:
   ```bash
   ./testing/track-b/step-B001-tests.sh
   ```

2. **Dev Server Verification**:
   Navigate to the frontend workspace and launch the Vite dev server:
   ```bash
   cd frontend
   npm run dev
   ```

---

## What "Pass" Looks Like

### 1. Test Script Output
Running `./testing/track-b/step-B001-tests.sh` must output:
```text
=========================================
Verifying STEP B001: Frontend Scaffold
=========================================
Running: npm run build...
...
✓ built in 134ms
PASS: npm run build completed successfully (Exit Code 0).
-----------------------------------------
All validations for STEP B001 PASSED.
=========================================
```

### 2. Dev Server Log Output
Launching `npm run dev` must output:
```text
  VITE v8.1.5  ready in 227 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

### 3. Browser Interface
When visiting `http://localhost:5173/`:
- The corporate landing page should render with a dark green logo and forest-themed palette.
- Clicking the "Verify API Configuration" button should display a browser alert stating:
  ```text
  Configuration Active:
  API Base URL: http://localhost:8080/api
  ```

---

## Mocking / Substitution Strategy
- **External services (S3, Gemini, SMTP, FastAPI)**: None active or integrated during this scaffold step. No mock servers need to be online.
