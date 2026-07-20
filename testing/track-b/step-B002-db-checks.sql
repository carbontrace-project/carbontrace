-- testing/track-b/step-B002-db-checks.sql
-- MOCK — replaced in B-SWAP-1
-- Note: Track B (Frontend) has no direct database access. 
-- In accordance with COMMANDO.md Section 22 (manual testing policy), database states 
-- are validated by checking mock server memory state changes reflected in response bodies
-- and checking mock server logs on port 8080.

-- 1. Assert Vendor Seeding & Listing State:
-- The mock server starts with 12 seeded vendors (satisfying the >= 12 pagination check).
-- To verify, run GET /api/vendors?page=0&size=5 and confirm 'totalElements' is 12:
-- Expected JSON: "totalElements": 12

-- 2. Assert In-Memory Vendor Insertion State:
-- Adding a vendor via POST /api/vendors appends it to mock server memory.
-- Subsequent GET /api/vendors?page=0&size=20 will contain the new vendor object, 
-- and totalElements will increment to 13.
-- Expected JSON: "totalElements": 13

-- 3. Assert Shipment Calculations State:
-- Running POST /api/shipments/:id/calculate updates the shipment status to 'CALCULATED'.
-- Retrieve the shipment details via GET /api/shipments/:id to confirm.
-- Expected JSON: "status": "CALCULATED"

-- 4. Assert Purchase Tonnes Decrement State:
-- Making a successful offset purchase against carbon credit listing 3 (requiredTonnes = 4.219)
-- decrements availableTonnes in listing 3 from 5200.000 to 5195.781.
-- Retrieve listings via GET /api/marketplace/credits/3 to verify.
-- Expected JSON: "availableTonnes": 5195.781
