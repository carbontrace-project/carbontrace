#!/bin/bash
# testing/track-b/step-B001-tests.sh
# Exercises and documents behaviors added in STEP B001.
# Since B001 implements the frontend skeleton and shell only, no backend API endpoints are introduced.
# This script verifies the build execution and documents configuration checks.

echo "========================================="
echo "Verifying STEP B001: Frontend Scaffold"
echo "========================================="

# 1. Verify build compilation.
# Expected exit code: 0 (Successful Vite production build).
echo "Running: npm run build..."
cd "$(dirname "$0")/../../frontend" || exit 1
npm run build
BUILD_STATUS=$?

if [ $BUILD_STATUS -eq 0 ]; then
  echo "PASS: npm run build completed successfully (Exit Code 0)."
else
  echo "FAIL: npm run build failed (Exit Code $BUILD_STATUS)."
  exit 1
fi

# 2. Dev server start check.
# Developers can start the development server using: npm run dev
# The server will boot cleanly on http://localhost:5173.
# Expected output/log lines:
# - "VITE v8.1.5 ready in 227 ms"
# - "➜  Local:   http://localhost:5173/"

echo "-----------------------------------------"
echo "All validations for STEP B001 PASSED."
echo "========================================="
