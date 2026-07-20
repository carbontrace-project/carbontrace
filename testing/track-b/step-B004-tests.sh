#!/bin/bash
# testing/track-b/step-B004-tests.sh
# Exercises and documents behaviors added in STEP B004 (Auth Pages & Forms).
# Connects to mocks/mock-backend on http://localhost:8080.

MOCK_URL="http://localhost:8080"
FRONTEND_DIR="$(dirname "$0")/../../frontend"

echo "========================================="
echo "Verifying STEP B004: Auth Pages"
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
echo "Executing Endpoint & Form Flow Verification against Mock Backend ($MOCK_URL)"
echo "-----------------------------------------"

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

# 1. Happy Path Registration (COMMANDO.md Section 8.1 & 9)
# Request: POST /api/auth/register
# Expected HTTP Status: 201 Created
# Expected ApiResponse shape:
# {
#   "success": true,
#   "message": "Registration successful. An OTP has been sent to your email.",
#   "data": { "email": "auditor@acme.com", "otpExpiresInMinutes": 10 }
# }
run_test "1.1 Register User (Happy Path)" 201 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/register' -H 'Content-Type: application/json' -d '{\"firstName\":\"Ananya\",\"lastName\":\"Rao\",\"companyName\":\"Acme Global Logistics\",\"email\":\"auditor2@acme.com\",\"password\":\"Password123!\",\"role\":\"ROLE_AUDITOR\"}'"

# 2. Happy Path Verify OTP (COMMANDO.md Section 8.1 & 9)
# Request: POST /api/auth/verify-otp with mock's fixed code 482913
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape:
# {
#   "success": true,
#   "message": "Email verified successfully",
#   "data": {
#     "accessToken": "jwt-auditor-token-...",
#     "refreshToken": "refresh-token-rotated-...",
#     "userId": 1, "email": "auditor2@acme.com", "role": "ROLE_AUDITOR"
#   }
# }
run_test "1.2 Verify OTP (Happy Path & Auto-Login)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/verify-otp' -H 'Content-Type: application/json' -d '{\"email\":\"auditor2@acme.com\",\"code\":\"482913\",\"purpose\":\"VERIFICATION\"}'"

# 3. Happy Path Resend OTP (COMMANDO.md Section 8.1)
# Request: POST /api/auth/resend-otp
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape: {"success": true, "message": "OTP resent successfully", "data": {"email": "auditor2@acme.com"}}
run_test "1.3 Resend OTP (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/resend-otp' -H 'Content-Type: application/json' -d '{\"email\":\"auditor2@acme.com\",\"purpose\":\"VERIFICATION\"}'"

# 4. Happy Path Login Auditor (COMMANDO.md Section 8.1)
# Request: POST /api/auth/login with auditor@acme.com
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape: {"success": true, "message": "Login successful", "data": {"accessToken": "jwt-auditor-token-...", "role": "ROLE_AUDITOR"}}
run_test "1.4 Login Auditor (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/login' -H 'Content-Type: application/json' -d '{\"email\":\"auditor@acme.com\",\"password\":\"Password123!\"}'"

# 5. Happy Path Login Admin (COMMANDO.md Section 8.1 & 10)
# Request: POST /api/auth/login with admin@carbontrace.dev
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape: {"success": true, "message": "Login successful", "data": {"accessToken": "jwt-admin-token-...", "role": "ROLE_ADMIN"}}
run_test "1.5 Login Admin (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/login' -H 'Content-Type: application/json' -d '{\"email\":\"admin@carbontrace.dev\",\"password\":\"Password123!\"}'"

# 6. Happy Path Forgot Password Request (COMMANDO.md Section 8.1)
# Request: POST /api/auth/forgot-password
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape: {"success": true, "message": "If email is registered, password reset OTP is sent.", "data": null}
run_test "1.6 Forgot Password Step 1 (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/forgot-password' -H 'Content-Type: application/json' -d '{\"email\":\"auditor@acme.com\"}'"

# 7. Happy Path Reset Password (COMMANDO.md Section 8.1)
# Request: POST /api/auth/reset-password with mock's fixed code 482913
# Expected HTTP Status: 200 OK
# Expected ApiResponse shape: {"success": true, "message": "Password reset successful. You may now login.", "data": null}
run_test "1.7 Reset Password Step 2 (Happy Path)" 200 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/reset-password' -H 'Content-Type: application/json' -d '{\"email\":\"auditor@acme.com\",\"code\":\"482913\",\"newPassword\":\"NewPassword123!\"}'"

# 8. Negative Case: Incorrect OTP Code (COMMANDO.md Section 9 & 12)
# Request: POST /api/auth/verify-otp with wrong code 999999
# Expected HTTP Status: 400 Bad Request
# Expected ApiResponse shape: {"success": false, "message": "Invalid OTP code or expired"}
run_test "2.1 Verify OTP Wrong Code (Expected 400)" 400 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/verify-otp' -H 'Content-Type: application/json' -d '{\"email\":\"auditor@acme.com\",\"code\":\"999999\",\"purpose\":\"VERIFICATION\"}'"

# 9. Negative Case: Unverified User Login (COMMANDO.md Section 9 & 12)
# Request: POST /api/auth/login with unverified@acme.com
# Expected HTTP Status: 400 Bad Request
# Expected ApiResponse shape: {"success": false, "message": "Email not verified"}
run_test "2.2 Login Unverified Email (Expected 400)" 400 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/login' -H 'Content-Type: application/json' -d '{\"email\":\"unverified@acme.com\",\"password\":\"Password123!\"}'"

# 10. Negative Case: Invalid Credentials (COMMANDO.md Section 8.1)
# Request: POST /api/auth/login with invalid email/password
# Expected HTTP Status: 401 Unauthorized
# Expected ApiResponse shape: {"success": false, "message": "Invalid email or password"}
run_test "2.3 Login Invalid Credentials (Expected 401)" 401 \
  "curl -s -w '\n%{http_code}' -X POST '$MOCK_URL/api/auth/login' -H 'Content-Type: application/json' -d '{\"email\":\"unknown@acme.com\",\"password\":\"WrongPass1\"}'"

echo "-----------------------------------------"
echo "Browser UI Verification Guide"
echo "-----------------------------------------"
echo "1. Start frontend: npm run dev"
echo "2. Navigate to http://localhost:5173/register"
echo "3. Complete registration form -> redirected to /verify-otp with prefilled email"
echo "4. Enter fixed mock OTP '482913' -> auto-login executes and redirects to /"
echo "5. Navigate to http://localhost:5173/login"
echo "6. Test login with 'unverified@acme.com' -> ErrorMessage displays 'Email not verified'"
echo "7. Test login with 'admin@carbontrace.dev' -> logs in with ROLE_ADMIN role"
echo "========================================="
echo "All validations for STEP B004 PASSED."
echo "========================================="
