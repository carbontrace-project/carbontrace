-- testing/track-b/step-B006-db-checks.sql
-- MOCK — replaced in B-SWAP-1
-- Note: Track B (Frontend) has no direct database connection.
-- Per COMMANDO.md Section 22 (testing policy), frontend state assertions are 
-- verified by inspecting mock backend memory state, response body shapes, and console log lines on port 8080.

-- 1. Dashboard Analytics Summary Assertion (COMMANDO.md Section 8.9 & 12):
-- GET /api/analytics/dashboard returns totalEmissionsKgco2e, netEmissionsKgco2e, monthlyEmissions object map, emissionsByMode, and emissionsByVendor array.

-- 2. GIS Map Points Assertion (COMMANDO.md Section 8.4 & 12):
-- GET /api/shipments/map returns array of CALCULATED shipments containing originLat, originLng, destinationLat, destinationLng, transportMode, and totalEmissionsKgco2e.

-- 3. Reduction Goals Assertion (COMMANDO.md Section 8.8 & 12):
-- POST /api/goals appends goal record. GET /api/goals computes progressPercent dynamically against baseline and current net annual emissions.

-- 4. Marketplace Inventory & Restock Assertion (COMMANDO.md Section 8.6 & 12):
-- GET /api/marketplace/listings returns credit listings array.
-- PUT /api/admin/marketplace/listings/1/restock increments availableTonnes.

-- 5. User Account Permissions Assertion (COMMANDO.md Section 8.10 & 10):
-- GET /api/admin/users returns accounts list for ROLE_ADMIN tokens only (403 for ROLE_AUDITOR).
-- PUT /api/admin/users/1/toggle-active for admin@carbontrace.dev returns 400 Bad Request ("Cannot deactivate own administrator account").
