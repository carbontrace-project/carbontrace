# MOCK — replaced in B-SWAP-1
# Mock CarbonTrace Spring Boot Backend

This mock backend server simulates all the endpoints of the Spring Boot corporate backend (as detailed in Sections 8.1 - 8.10 of `COMMANDO.md`). It operates locally on port `8080`, allowing the React frontend (`frontend/`) to be fully tested and developed in isolation without needing a live AWS, Gmail, or database setup.

## Port
`8080`

## Stands in For
* **Authentication APIs:** Register, verify-otp, resend-otp, login, refresh tokens, forgot-password, reset-password.
* **User Management APIs:** Profile retrieval & updates, and Admin User management (listing & toggle-active).
* **Vendor APIs:** Creating, listing, pagination, and toggling active state.
* **Shipment APIs:** S3 upload presigning, mock file ingestion, extraction review, distance and emission calculations, and geo-data mapping.
* **Emission Factor reference APIs:** Admin CRUD and pagination.
* **Marketplace Listings APIs:** Project and seller browsing.
* **Purchase APIs:** Initiating offset purchases using the autonomous agent (simulated).
* **Goal Management APIs:** Creating, updating, deleting carbon reduction goals.
* **Analytics Dashboard APIs:** Fetching live aggregate totals, monthly emission charts, and goals progress.

## Retiring Step
This mock is fully retired in **B-SWAP-1** when configuration points the frontend base URL to the real Spring Boot service.

## Prerequisites
* **Node.js**: Version 22 LTS or newer.

## How to Run

1. Navigate to the directory:
   ```bash
   cd mocks/mock-backend
   ```
2. Install dependencies (if not already done):
   ```bash
   npm install
   ```
3. Launch the mock server:
   ```bash
   npm start
   ```

The console will print:
`[MOCK BACKEND] CarbonTrace Spring Boot Server listening on port 8080`
