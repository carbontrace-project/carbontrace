-- testing/track-b/step-B001-db-checks.sql
-- Note: Track B (Frontend) has no direct database connection.
-- Instead, we substitute database queries with assertions on the build outputs, configuration environment, and page rendering.

-- Assertion 1: Build Output Verification
-- Expected: The production build output contains index.html and assets/ folder containing compiled JS & CSS.
-- Log Check: dist/index.html, dist/assets/index-*.js, and dist/assets/index-*.css must be present.

-- Assertion 2: Environment Configuration Verification
-- Expected: VITE_API_BASE_URL evaluates to 'http://localhost:8080/api' in the environment.
-- Code Check: import.meta.env.VITE_API_BASE_URL must equal 'http://localhost:8080/api'.

-- Assertion 3: Routing Architecture Verification
-- Expected: A router provider must be set up with a single '/' index path rendering the Home page shell.
-- Code Check: App.jsx initializes createBrowserRouter with path: '/' rendering Home.

-- Assertion 4: Design Token Verification
-- Expected: The global stylesheet contains CSS custom properties for sustainability theme variables.
-- Code Check: global.css defines --primary, --primary-light, --accent, --accent-light, --bg-base, --space-*, --font-*.
