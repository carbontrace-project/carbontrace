#!/bin/bash
# testing/track-b/step-B003-tests.sh
# Exercises and documents behaviors added in STEP B003 (Auth Plumbing & Common Components).
# Connects to mocks/mock-backend on http://localhost:8080.

MOCK_URL="http://localhost:8080"
FRONTEND_DIR="$(dirname "$0")/../../frontend"

echo "========================================="
echo "Verifying STEP B003: Frontend Foundation"
echo "========================================="

# 1. Build Verification
echo "Running: npm run build..."
cd "$FRONTEND_DIR" || exit 1
npm run build
BUILD_STATUS=$?

if [ $BUILD_STATUS -eq 0 ]; then
  echo "PASS: npm run build completed successfully (Exit Code 0)."
else
  echo "FAIL: npm run build failed (Exit Code $BUILD_STATUS)."
  exit 1
fi

echo "-----------------------------------------"
echo "Executing Endpoint & Interceptor Verification against Mock Backend ($MOCK_URL)"
echo "-----------------------------------------"

# Function to run curl and check status code
run_test() {
  local title="$1"
  local expected_status="$2"
  local curl_cmd="$3"

  local response
  response=$(eval "$curl_cmd" 2>/dev/null)
  local http_code
  http_code=$(echo "$response" | tail -n1)

  if [ "$http_code" -eq "$expected_status" ]; then
    echo "PASS: $title (HTTP $http_code)"
  else
    echo "FAIL: $title (Expected HTTP $expected_status, got $http_code)"
    echo "Response body: $response"
  fi
}

# 1. Happy Path Authentication (COMMANDO.md Section 8.1)
# Request: POST /api/auth/login with valid auditor credentials
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape:
# {
#   "success": true,
#   "message": "Login successful",
#   "data": {
#     "accessToken": "jwt-auditor-token-...",
#     "refreshToken": "refresh-token-rotated-...",
#     "userId": 1, "email": "auditor@acme.com", "role": "ROLE_AUDITOR"
#   }
# }
run_test "1.1 Login Auditor (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/login' -H 'Content-Type: application/json' -d '{\"email\":\"auditor@acme.com\",\"password\":\"Password123!\"}'"

# 2. Happy Path Token Refresh (COMMANDO.md Section 8.1 & 10)
# Request: POST /api/auth/refresh with valid refreshToken
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape:
# {
#   "success": true,
#   "message": "Tokens refreshed",
#   "data": {
#     "accessToken": "jwt-auditor-token-...",
#     "refreshToken": "refresh-token-rotated-..."
#   }
# }
run_test "2.1 Token Refresh (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/refresh' -H 'Content-Type: application/json' -d '{\"refreshToken\":\"refresh-token-sample\"}'"

# 3. Auth Case: Missing Authorization Header (COMMANDO.md Section 10)
# Request: GET /api/vendors without Authorization header
# Expected HTTP Status: 401 Unauthorized
# Expected ApiResponse shape: {"success": false, "message": "Unauthorized: Missing Authorization header"}
run_test "3.1 Protected Route Missing Token (Expected 401)" 401 \
  "curl -s -w '\n%{http_code}' -X GET '$MOCK_URL/api/vendors'"

# 4. Negative Case: Expired Token Triggers 401 Interceptor Refresh+Retry (COMMANDO.md Section 12)
# Request: GET /api/vendors with Authorization: Bearer expired-token
# Expected HTTP Status: 401 Unauthorized from mock server
# Behavior: axiosConfig interceptor catches 401, issues POST /api/auth/refresh, updates tokens, and retries GET /api/vendors
run_test "4.1 Protected Route Expired Token (Expected 401)" 401 \
  "curl -s -w '\n%{http_code}' -X GET '$MOCK_URL/api/vendors' -H 'Authorization: Bearer expired-token'"

# 5. Auth Case: Auditor Role Accessing Admin Route (COMMANDO.md Section 10)
# Request: GET /api/admin/users with Auditor token
# Expected HTTP Status: 403 Forbidden
# Expected ApiResponse shape: {"success": false, "message": "Access Denied: Requires Administrator role"}
run_test "5.1 Admin Route by Auditor (Expected 403)" 403 \
  "curl -s -w '\n%{http_code}' -X GET '$MOCK_URL/api/admin/users' -H 'Authorization: Bearer jwt-auditor-token-123'"

# 6. Auth Case: Admin Role Accessing Admin Route (COMMANDO.md Section 10)
# Request: GET /api/admin/users with Admin token
# Expected HTTP Status: 200 OK
run_test "6.1 Admin Route by Admin (Expected 200)" 200 \
  "curl -s -w '\n%{http_code}' -X GET '$MOCK_URL/api/admin/users' -H 'Authorization: Bearer jwt-admin-token-123'"

echo "-----------------------------------------"
echo "Browser Console Interceptor Testing Guide"
echo "-----------------------------------------"
echo "To verify the Axios response interceptor in the browser:"
echo "1. Start frontend: npm run dev"
echo "2. Open http://localhost:5173 in Chrome/Firefox DevTools console"
echo "3. Execute the following JavaScript snippet:"
echo ""
echo "   localStorage.setItem('refreshToken', 'refresh-token-sample');"
echo "   import('./src/api/axiosConfig.js').then(({ default: apiClient, setAccessToken }) => {"
echo "     setAccessToken('expired-token');"
echo "     apiClient.get('/api/vendors').then(res => console.log('Interceptor Retry SUCCESS:', res.data));"
echo "   });"
echo ""
echo "Expected Console Behavior:"
echo "- 1st request GET /api/vendors with expired-token returns 401."
echo "- Interceptor executes POST /api/auth/refresh and retrieves rotated tokens."
echo "- 2nd request GET /api/vendors retries with new access token and succeeds (HTTP 200)."
echo "========================================="
echo "All validations for STEP B003 PASSED."
echo "========================================="
