-- testing/track-b/step-B003-db-checks.sql
-- MOCK — replaced in B-SWAP-1
-- Note: Track B (Frontend) has no direct database connection.
-- Per COMMANDO.md Section 22 (testing policy), frontend database assertions are 
-- verified by inspecting mock backend memory state, response body shapes, and console log lines on port 8080.

-- 1. Token Refresh Assertion (COMMANDO.md Section 8.1 & 10):
-- Invoking POST /api/auth/refresh returns a refreshed access token and rotated refresh token.
-- Verify that response contains 'accessToken' and 'refreshToken' keys inside data payload.
-- Expected Response Body:
-- {
--   "success": true,
--   "message": "Tokens refreshed",
--   "data": {
--     "accessToken": "jwt-auditor-token-...",
--     "refreshToken": "refresh-token-rotated-..."
--   }
-- }

-- 2. Expired Token Refresh+Retry Assertion (COMMANDO.md Section 12):
-- Making an API request with 'expired-token' returns 401 Unauthorized.
-- The response interceptor must catch 401, call /api/auth/refresh, rotate tokens, and retry original request.
-- Verify server logs show the sequential pattern:
--   [MOCK BACKEND] GET /api/vendors (Returns 401)
--   [MOCK BACKEND] POST /api/auth/refresh (Returns 200 with rotated tokens)
--   [MOCK BACKEND] GET /api/vendors (Retried request returns 200 OK)

-- 3. Unauthenticated State & Logout Cleanup Assertion (COMMANDO.md Section 10):
-- On refresh failure or expired session, 'refreshToken' is removed from localStorage, 
-- in-memory 'accessToken' is cleared, and user is redirected to '/login'.
-- Verify via browser console:
--   localStorage.getItem('refreshToken') === null

-- 4. Role Authorization Assertion (COMMANDO.md Section 10 Security Filter Chain):
-- Non-admin role attempting access to '/api/admin/users' receives 403 Forbidden response.
-- Expected Response Body:
-- {
--   "success": false,
--   "message": "Access Denied: Requires Administrator role"
-- }
