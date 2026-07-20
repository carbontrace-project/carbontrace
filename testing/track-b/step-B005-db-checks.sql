-- testing/track-b/step-B005-db-checks.sql
-- MOCK — replaced in B-SWAP-1
-- Note: Track B (Frontend) has no direct database connection.
-- Per COMMANDO.md Section 22 (testing policy), frontend state assertions are 
-- verified by inspecting mock backend memory state, response body shapes, and console log lines on port 8080.

-- 1. Vendors Registry Assertion (COMMANDO.md Section 8.2 & 12):
-- GET /api/vendors returns 12 seeded logistics vendors.
-- POST /api/vendors appends a new vendor to in-memory state.
-- PUT /api/vendors/1/toggle-active by ROLE_ADMIN toggles the vendor's isActive boolean.

-- 2. Direct S3 Upload & Shipment Extraction Assertion (COMMANDO.md Section 8.3 & 12):
-- POST /api/shipments/upload-url generates presigned uploadUrl and s3Key.
-- Bare PUT /mock-s3/... accepts PDF bytes without Authorization header.
-- POST /api/shipments creates shipment record in NEEDS_REVIEW status with extracted fields and fieldConfidence map.

-- 3. Extraction Review & Emission Calculation Assertion (COMMANDO.md Section 8.4 & 12):
-- PUT /api/shipments/1/review updates extracted fields and advances status to REVIEWED.
-- POST /api/shipments/1/calculate computes transport distance & emissions, updating status to CALCULATED.
-- POST /api/shipments/502/calculate triggers designated 502 Bad Gateway response.

-- 4. Offset Purchase Assertion (COMMANDO.md Section 8.7 & 12):
-- POST /api/purchases with maxBudgetUsd > 20 matches listing 3 and creates purchase record with status COMPLETED and transactionReference SIM-XXXXXX.
-- POST /api/purchases with maxBudgetUsd <= 20 returns decision NO_PURCHASE with agent reasoning.
