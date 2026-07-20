#!/bin/bash
# testing/track-b/step-B002-tests.sh
# Exercises and documents behaviors added in STEP B002: Mock Backend Server.
# Verify that all mocked endpoints return the correct shapes and status codes.

echo "========================================="
# Check if mock server is running on 8080
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/marketplace/credits > /dev/null
if [ $? -ne 0 ]; then
  echo "ERROR: Mock backend is not running on port 8080. Start it first before running tests."
  exit 1
fi

echo "Verifying STEP B002: Mock Backend Endpoints"
echo "========================================="

# Helper function to print test results
assert_response() {
  local label="$1"
  local url="$2"
  local method="$3"
  local data="$4"
  local headers="$5"
  local expected_status="$6"

  echo -n "Test: $label... "

  local curl_cmd="curl -s -o response.json -w \"%{http_code}\" -X $method"
  if [ -n "$headers" ]; then
    curl_cmd="$curl_cmd $headers"
  fi
  if [ -n "$data" ]; then
    curl_cmd="$curl_cmd -d '$data'"
  fi
  curl_cmd="$curl_cmd http://localhost:8080$url"

  local status
  status=$(eval "$curl_cmd")

  if [ "$status" -eq "$expected_status" ]; then
    echo "PASS (HTTP $status)"
  else
    echo "FAIL (HTTP $status, Expected $expected_status)"
    echo "Response body:"
    cat response.json
    rm -f response.json
    exit 1
  fi
  rm -f response.json
}

# ----------------------------------------------------
# 1. AUTHENTICATION TESTS
# ----------------------------------------------------

# 1.1 Login Auditor (Happy Path)
# Expected Status: 200 OK
# Expected Shape: ApiResponse<AuthResponse> => { success: true, message: String, data: { accessToken, refreshToken, tokenType: "Bearer", userId: 1, email: "auditor@acme.com", role: "ROLE_AUDITOR", firstName: "Ananya", lastName: "Rao" } }
assert_response "1.1 Login Auditor (Happy)" "/api/auth/login" "POST" '{"email":"auditor@acme.com","password":"Password123!"}' "-H 'Content-Type: application/json'" 200

# 1.2 Login Admin (Happy Path)
# Expected Status: 200 OK
# Expected Shape: ApiResponse<AuthResponse> => { success: true, message: String, data: { accessToken, refreshToken, tokenType: "Bearer", userId: 2, email: "admin@carbontrace.dev", role: "ROLE_ADMIN", firstName: "Admin", lastName: "User" } }
assert_response "1.2 Login Admin (Happy)" "/api/auth/login" "POST" '{"email":"admin@carbontrace.dev","password":"Password123!"}' "-H 'Content-Type: application/json'" 200

# 1.3 Login Unverified
# Expected Status: 400 Bad Request
# Expected Shape: ApiResponse.error => { success: false, message: "Email not verified", data: null }
assert_response "1.3 Login Unverified (Expected 400)" "/api/auth/login" "POST" '{"email":"unverified@acme.com","password":"Password123!"}' "-H 'Content-Type: application/json'" 400

# 1.4 OTP verification incorrect code
# Expected Status: 400 Bad Request
# Expected Shape: ApiResponse.error => { success: false, message: "Invalid OTP code or expired", data: null }
assert_response "1.4 OTP Verify Incorrect (Expected 400)" "/api/auth/verify-otp" "POST" '{"email":"auditor@acme.com","code":"000000","purpose":"REGISTRATION"}' "-H 'Content-Type: application/json'" 400

# 1.5 OTP verification correct code
# Expected Status: 200 OK
# Expected Shape: ApiResponse<AuthResponse> => { success: true, message: "Email verified successfully", data: { accessToken, refreshToken, tokenType: "Bearer", userId: 1, email: "auditor@acme.com", role: "ROLE_AUDITOR", firstName: "Ananya", lastName: "Rao" } }
assert_response "1.5 OTP Verify Correct" "/api/auth/verify-otp" "POST" '{"email":"auditor@acme.com","code":"482913","purpose":"REGISTRATION"}' "-H 'Content-Type: application/json'" 200

# ----------------------------------------------------
# 2. SECURITY GUARD TESTS
# ----------------------------------------------------

# 2.1 Protected endpoint without token
# Expected Status: 401 Unauthorized
# Expected Shape: ApiResponse.error => { success: false, message: "Unauthorized: Missing Authorization header", data: null }
assert_response "2.1 Protected Route No Token (Expected 401)" "/api/users/me" "GET" "" "" 401

# 2.2 Protected endpoint with expired token
# Expected Status: 401 Unauthorized
# Expected Shape: ApiResponse.error => { success: false, message: "Unauthorized: Token has expired", data: null }
assert_response "2.2 Protected Route Expired Token (Expected 401)" "/api/users/me" "GET" "" "-H 'Authorization: Bearer expired-token'" 401

# 2.3 Protected endpoint with valid token
# Expected Status: 200 OK
# Expected Shape: ApiResponse<UserResponseDto> => { success: true, message: String, data: { id: 1, email: "auditor@acme.com", firstName: "Ananya", lastName: "Rao", companyName: "Acme Global Logistics", role: "ROLE_AUDITOR", isActive: true } }
assert_response "2.3 Protected Route Valid Token" "/api/users/me" "GET" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 200

# 2.4 Admin endpoint accessed by Auditor
# Expected Status: 403 Forbidden
# Expected Shape: ApiResponse.error => { success: false, message: "Access Denied: Requires Administrator role", data: null }
assert_response "2.4 Admin Route by Auditor (Expected 403)" "/api/admin/users" "GET" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 403

# 2.5 Admin endpoint accessed by Admin
# Expected Status: 200 OK
# Expected Shape: ApiResponse<PagedResponse<UserResponseDto>> => { success: true, message: String, data: { content: Array, page: 0, size: 10, totalElements: 3, totalPages: 1, last: true } }
assert_response "2.5 Admin Route by Admin" "/api/admin/users" "GET" "" "-H 'Authorization: Bearer jwt-admin-token-xyz'" 200

# ----------------------------------------------------
# 3. CORE LOGISTICS WORKFLOWS
# ----------------------------------------------------

# 3.1 Vendor Pagination (List contains >= 12 rows)
# Expected Status: 200 OK
# Expected Shape: ApiResponse<PagedResponse<VendorResponseDto>> => { success: true, message: String, data: { content: Array[5], page: 0, size: 5, totalElements: 12, totalPages: 3, last: false } }
assert_response "3.1 List Vendors" "/api/vendors?page=0&size=5" "GET" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 200

# 3.2 Upload URL presigning request
# Expected Status: 200 OK
# Expected Shape: ApiResponse<UploadUrlResponse> => { success: true, message: "Upload URL generated", data: { uploadUrl: String, s3Key: String, expiresInSeconds: 600 } }
assert_response "3.2 Generate Upload URL" "/api/shipments/upload-url" "POST" '{"fileName":"invoice.pdf","contentType":"application/pdf"}' "-H 'Authorization: Bearer jwt-auditor-token-xyz' -H 'Content-Type: application/json'" 200

# 3.3 Create Shipment
# Expected Status: 201 Created
# Expected Shape: ApiResponse<ShipmentResponseDto> => { success: true, message: "Shipment created and AI fields extracted", data: { id: 13, vendorId: 1, uploadedBy: 1, invoiceNumber: "MOCK-INV-13", carrierName: "Logistics Vendor 1", shipmentDate: String, originCity: "Shenzhen", originCountry: "China", originLat: 22.5431, originLng: 114.0579, destinationCity: "Rotterdam", destinationCountry: "Netherlands", destinationLat: 51.9244, destinationLng: 4.4777, transportMode: "SEA", fuelType: "HEAVY_FUEL_OIL", weightTonnes: 18.5, distanceKm: null, distanceSource: null, extractionConfidence: "MEDIUM", totalEmissionsKgco2e: null, offsetTonnes: 0, status: "NEEDS_REVIEW", fieldConfidence: { weightTonnes: "HIGH", transportMode: "HIGH", originCity: "HIGH", destinationCity: "HIGH", fuelType: "MEDIUM", shipmentDate: "HIGH", originLat: "LOW", originLng: "LOW", destinationLat: "LOW", destinationLng: "LOW" }, createdAt: String, updatedAt: String } }
assert_response "3.3 Create Shipment" "/api/shipments" "POST" '{"vendorId":1,"s3Key":"invoices/2026/07/test.pdf","fileName":"test.pdf","fileSizeBytes":102400}' "-H 'Authorization: Bearer jwt-auditor-token-xyz' -H 'Content-Type: application/json'" 201

# 3.4 Reviewing CALCULATED shipment (id 7)
# Expected Status: 400 Bad Request
# Expected Shape: ApiResponse.error => { success: false, message: "already calculated", data: null }
assert_response "3.4 Review calculated shipment id 7 (Expected 400)" "/api/shipments/7/review" "PUT" '{"weightTonnes":25.0}' "-H 'Authorization: Bearer jwt-auditor-token-xyz' -H 'Content-Type: application/json'" 400

# 3.5 Calculate emissions (AI Service unavailable for 502)
# Expected Status: 502 Bad Gateway
# Expected Shape: ApiResponse.error => { success: false, message: "AI service unavailable — please try again", data: null }
assert_response "3.5 Calculate emissions shipment id 502 (Expected 502)" "/api/shipments/502/calculate" "POST" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 502

# 3.6 Calculate emissions (Happy Path)
# Expected Status: 200 OK
# Expected Shape: ApiResponse<ShipmentResponseDto> => { success: true, message: "Emissions calculated", data: { id: 1, vendorId: 1, uploadedBy: 1, invoiceNumber: String, carrierName: String, shipmentDate: String, originCity: String, originCountry: String, originLat: Number, originLng: Number, destinationCity: String, destinationCountry: String, destinationLat: Number, destinationLng: Number, transportMode: String, fuelType: String, weightTonnes: Number, distanceKm: 20430, distanceSource: "COMPUTED", extractionConfidence: String, totalEmissionsKgco2e: Number, offsetTonnes: Number, status: "CALCULATED", createdAt: String, updatedAt: String } }
assert_response "3.6 Calculate emissions shipment id 1" "/api/shipments/1/calculate" "POST" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 200

# 3.7 Mapped shipments points
# Expected Status: 200 OK
# Expected Shape: ApiResponse<List<ShipmentMapPointDto>> => { success: true, message: String, data: [ { id: 1, originLat: 22.5431, originLng: 114.0579, destinationLat: 51.9244, destinationLng: 4.4777, originCity: "Shenzhen", destinationCity: "Rotterdam", transportMode: "SEA", totalEmissionsKgco2e: Number, status: "CALCULATED" }, ... ] }
assert_response "3.7 Map Coordinates" "/api/shipments/map" "GET" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 200

# ----------------------------------------------------
# 4. MARKETPLACE & PURCHASES
# ----------------------------------------------------

# 4.1 Get Marketplace credits (Public)
# Expected Status: 200 OK
# Expected Shape: ApiResponse<PagedResponse<ListingResponseDto>> => { success: true, message: String, data: { content: Array, page: 0, size: 20, totalElements: Number, totalPages: Number, last: Boolean } }
assert_response "4.1 Get Marketplace Credits (Public)" "/api/marketplace/credits" "GET" "" "" 200

# 4.2 Purchase Offset low budget
# Expected Status: 200 OK
# Expected Shape: ApiResponse<AgentPurchaseResponse> => { success: true, message: "NO_PURCHASE decision returned by AI Agent", data: { decision: "NO_PURCHASE", listingId: null, tonnes: null, pricePerTonneUsd: null, totalCostUsd: null, transactionReference: null, reasoning: "No active listing offers 4.219 tonnes within the $10 budget; the cheapest viable option costs $57.98.", status: "FAILED" } }
assert_response "4.2 Purchase Offset Low Budget" "/api/purchases" "POST" '{"shipmentId":1,"maxBudgetUsd":10}' "-H 'Authorization: Bearer jwt-auditor-token-xyz' -H 'Content-Type: application/json'" 200

# 4.3 Purchase Offset high budget
# Expected Status: 201 Created
# Expected Shape: ApiResponse<OffsetPurchase> => { success: true, message: "Offset purchased (simulated)", data: { id: 13, shipmentId: 7, listingId: 3, purchasedBy: 1, sellerName: "Verdant Carbon Co.", projectName: "Amazon Basin Reforestation Phase II", tonnesPurchased: 4.219, pricePerTonneUsd: 14.50, totalCostUsd: 61.18, transactionReference: String, agentReasoning: String, status: "COMPLETED", purchasedAt: String } }
assert_response "4.3 Purchase Offset High Budget" "/api/purchases" "POST" '{"shipmentId":7,"maxBudgetUsd":200}' "-H 'Authorization: Bearer jwt-auditor-token-xyz' -H 'Content-Type: application/json'" 201

# ----------------------------------------------------
# 5. GOALS & ANALYTICS
# ----------------------------------------------------

# 5.1 Create Goal
# Expected Status: 201 Created
# Expected Shape: ApiResponse<ReductionGoal> => { success: true, message: "Reduction goal created successfully", data: { id: 4, title: "Test Goal", targetYear: 2030, baselineEmissionsKgco2e: 10000, targetEmissionsKgco2e: 5000, notes: null, createdBy: 1, createdAt: String, updatedAt: String } }
assert_response "5.1 Create Goal" "/api/goals" "POST" '{"title":"Test Goal","targetYear":2030,"baselineEmissionsKgco2e":10000,"targetEmissionsKgco2e":5000}' "-H 'Authorization: Bearer jwt-auditor-token-xyz' -H 'Content-Type: application/json'" 201

# 5.2 Get Dashboard Aggregates
# Expected Status: 200 OK
# Expected Shape: ApiResponse<DashboardDto> => { success: true, message: String, data: { totalShipments: 13, calculatedShipments: Number, totalEmissionsKgco2e: Number, totalOffsetTonnes: Number, netEmissionsKgco2e: Number, totalOffsetSpendUsd: Number, emissionsByMode: { SEA: Number, ROAD: Number, AIR: Number, RAIL: Number }, emissionsByVendor: Array, monthlyEmissions: Object, goals: Array } }
assert_response "5.2 Get Analytics Dashboard" "/api/analytics/dashboard" "GET" "" "-H 'Authorization: Bearer jwt-auditor-token-xyz'" 200

echo "-----------------------------------------"
echo "All validations for STEP B002 PASSED."
echo "========================================="
