# CarbonTrace — Complete Build Guide (Implementation Playbook)

**Companion to:** `COMMANDO.md` (the Commando File / Instructions File)
**Purpose:** Build the entire CarbonTrace project from zero using ONLY sequential prompts to Claude Code / Antigravity IDE.
**Build order:** THREE PARALLEL TRACKS (COMMANDO.md Section 25) + shared Integration and Final phases:

- **Track A — Backend + AWS** (Phases A1–A5, STEPS A001–A027, then swap steps A-SWAP-1..3) — owns `backend/`, all AWS/Gmail manual setups, `testing/track-a/`
- **Track B — Frontend** (Phase B1, STEPS B001–B006, then B-SWAP-1) — owns `frontend/`, `testing/track-b/`
- **Track C — AI Service** (Phase C1, STEPS C001–C005, then C-SWAP-1) — owns `ai-service/`, `testing/track-c/`
- **Shared Integration** (STEPS I001–I003) and **Shared Final** (STEPS F001–F003) — all three members together on `main`

All three developers use this same guide. Each starts at STEP A001 / B001 / C001 of their own track immediately. No track ever waits for another: every cross-track dependency is served by a mock from COMMANDO.md Section 25.3 until the explicitly labeled integration-swap step (A-SWAP-1/2/3, B-SWAP-1, C-SWAP-1) replaces it with the real service.
**Rule of thumb:** Every step is a 30–90 minute chunk. The step's own track must compile and run after every step. Never skip ahead within your track. Never reorder.

### Step Renumbering Map (old sequential guide → parallel tracks)

| Old step | New step id |
|---|---|
| STEP 001–019 | STEP A001–A019 (content unchanged; Track A) |
| STEP 020 | STEP A021 — CalculationService |
| STEP 021 | STEP A022 — map endpoint |
| STEP 022 | STEP A023 — goal module |
| STEP 023 | STEP A024 — analytics |
| STEP 024 | STEP A025 — marketplace |
| STEP 025 | STEP A026 — purchase commit path |
| STEP 026 | STEP A027 — admin module |
| STEP 027 | STEP B001 (Vite scaffold) + STEP B003 (auth plumbing + common components) |
| STEP 028 | STEP B004 — auth pages |
| STEP 029 | STEP B005 — vendors/upload/shipments UI |
| STEP 030 | STEP B006 — remaining pages |
| STEP 031 | STEP C001 — FastAPI setup |
| STEP 032 | STEP C003 — calculator + /calculate |
| STEP 033 | STEP C004 — extraction pipeline |
| STEP 034 | A-SWAP-1 (moved to Track A — extraction wiring is backend work) |
| STEP 035 | A-SWAP-2 (backend half; the FastAPI-in-isolation half is covered in STEP C003) |
| STEP 036 | STEP C005 — purchase agent |
| STEP 037 | STEP I001 (its backend agent-wiring precondition became A-SWAP-3) |
| STEP 038 | STEP I002 — full smoke test |
| STEP 039 | STEP I003 — edge cases |
| STEP 040–042 | STEP F001–F003 — shared final phase |
| — (new) | STEP A020 (build `mocks/mock-ai/`), STEP B002 (build `mocks/mock-backend/`), STEP C002 (build `mocks/mock-marketplace/` + `mocks/sample-pdfs/`), A-SWAP-3, B-SWAP-1, C-SWAP-1 |

---

## SECTION 0 — HOW TO USE THIS GUIDE

### 0.1 The Workflow for EVERY Step

Each developer runs this loop over THEIR OWN track's steps only (Track A: A001→…, Track B: B001→…, Track C: C001→…), on their own branch (`track-a`, `track-b`, `track-c`). Do not shortcut it.

```
1. (If listed) Complete the MANUAL SETUP block yourself and verify it.
2. Paste the SESSION INIT PROMPT (0.2) if this is a fresh AI session.
3. Paste the step's IMPLEMENTATION PROMPT. Let it finish.
4. Paste the step's VERIFICATION PROMPT (built from your track's template: 0.3 for
   Track A, 0.3B for Track B, 0.3C for Track C; shared I/F steps use the variant
   matching the service under review).
5. Paste the step's TESTING PROMPT (built from the template in 0.4).
6. Run the MANUAL TESTING commands yourself.
7. Tick every box in SUCCESS CRITERIA. If anything fails, use the FIX PROMPT (0.5).
8. Commit on your track branch: git add -A && git commit -m "A013: <title>" (your
   step id as the message prefix). Track branches merge to `main` at the
   integration checkpoints (your swap steps and the start of the Shared
   Integration phase).
9. Move to the step named in NEXT STEP.
```

### 0.2 Session Initialization Prompt (paste at the start of EVERY new AI session)

```
Read the COMMANDO.md file and analyze the entire existing project codebase
before generating any code. Understand the current architecture, folder
structure, completed modules, dependencies, coding conventions, and
implementation progress. Continue only from the requested implementation step,
preserving all existing code and project structure. Do not regenerate, refactor,
overwrite, or modify working code unless the current step explicitly requires
it. Ensure all newly generated code integrates seamlessly with the existing
project and maintains consistency throughout the application.
```

### 0.3 Standard Verification Prompt (Track A — Spring Boot template)

Track A steps fill the two placeholders below. Track B steps use 0.3B; Track C steps use 0.3C; shared I/F steps use the variant matching the service under review. Paste the whole thing.

```
Act as a strict senior code reviewer. Review ONLY the work done in {STEP_ID}: {STEP_SCOPE}.
Read COMMANDO.md first and verify full compliance. Check every item below and report
PASS/FAIL per item with file/line references. Fix only genuine violations; change nothing else.

1. Architecture consistency: layered flow Controller → Service → Repository → Database only;
   no events, queues, schedulers, background jobs, CQRS, or extra design patterns.
2. Coding standards (COMMANDO.md Section 21): constructor injection via @RequiredArgsConstructor
   only (no field @Autowired); interface + impl for every service; MapStruct for all mapping;
   Lombok annotations on entities; @Enumerated(EnumType.STRING) for enums; BigDecimal for
   money/tonnes; @Slf4j logging.
3. Naming conventions: package structure exactly as COMMANDO.md Section 6; DTO/entity/mapper
   names match the spec.
4. API contracts: request/response shapes, status codes, and ApiResponse<T> wrapper exactly
   match COMMANDO.md Section 8 for the endpoints in scope.
5. Error handling: correct exceptions thrown; GlobalExceptionHandler mappings per Section 23;
   error responses use the ApiResponse error format.
6. Logging: meaningful logs at INFO/ERROR; NO passwords, OTP codes, JWTs, AWS credentials,
   or full presigned URLs logged.
7. Validation: Jakarta validation on request DTOs mirrors Section 9 business rules.
8. Security: endpoint authorization matches Section 10's filter chain table; no secrets
   hardcoded; credentials only from environment variables.
9. Edge cases: the failure paths listed for this step are actually handled.
10. No unnecessary abstractions: no factories, abstract base classes, extra layers, or helpers
    the spec does not require.
11. Scope discipline: NO future-step functionality was implemented, no placeholder/TODO code,
    every method fully implemented.
12. Compilation: run the build command for the affected service and confirm it succeeds.

{STEP_EXTRA_CHECKS}
```

### 0.3B Standard Verification Prompt (Track B — Frontend template)

Track B steps use this instead of 0.3. Paste the whole thing.

```
Act as a strict senior code reviewer. Review ONLY the work done in {STEP_ID}: {STEP_SCOPE}.
Read COMMANDO.md first and verify full compliance. Check every item below and report
PASS/FAIL per item with file/line references. Fix only genuine violations; change nothing else.

1. State strategy (COMMANDO.md Section 12): auth state ONLY in React Context
   (AuthContext); everything else local component state + Axios calls; NO Redux,
   NO React Query.
2. Styling: CSS Modules only — one module per component/page, shared tokens as CSS
   custom properties in global.css; NO Tailwind, NO UI kits.
3. HTTP: all calls through the shared Axios instance; request interceptor attaches the
   Bearer token; response interceptor on 401 calls /api/auth/refresh ONCE (refresh token
   from localStorage), retries the original request, logs out on failure. The presigned
   S3 PUT is the only bare (non-intercepted) request and carries NO Authorization header.
4. Forms: React Hook Form everywhere, client validation mirroring COMMANDO.md Section 9.
5. Dependencies: package.json contains ONLY the Section 4 frontend packages.
6. API contracts: request/response handling matches COMMANDO.md Section 8 exactly
   (camelCase keys, ApiResponse/PagedResponse shapes, status-code handling).
7. Naming/structure: folder layout and file names per Section 6; PascalCase components,
   camelCase utils; functional components only.
8. UX rules: loading (LoadingSpinner), error (ErrorMessage), and empty (EmptyState)
   states on every page; number formatting per Section 12 (kgCO₂e thousands separators,
   USD 2dp, tonnes 3dp); SIMULATED labels on every purchase/marketplace surface.
9. Security: access token in memory only (never localStorage); route guards
   (ProtectedRoute/AdminRoute) per Section 10 roles; no secrets committed.
10. Edge cases: the failure paths listed for this step are actually handled.
11. Scope discipline: NO future-step functionality, no placeholder/TODO code, every
    component fully implemented. Production code never imports from mocks/ — only which
    server listens on VITE_API_BASE_URL decides mock vs real.
12. Compilation: npm run build succeeds.

{STEP_EXTRA_CHECKS}
```

### 0.3C Standard Verification Prompt (Track C — FastAPI template)

Track C steps use this instead of 0.3. Paste the whole thing.

```
Act as a strict senior code reviewer. Review ONLY the work done in {STEP_ID}: {STEP_SCOPE}.
Read COMMANDO.md first and verify full compliance. Check every item below and report
PASS/FAIL per item with file/line references. Fix only genuine violations; change nothing else.

1. Structure (COMMANDO.md Sections 6, 13): flat layout exactly as specified — NO service
   layers, NO dependency-injection containers, NO repositories in FastAPI.
2. Boundaries: NO database access of any kind; NO AWS SDK and NO AWS credentials —
   documents arrive only as URLs in request payloads; all outputs return in the HTTP
   response; Spring Boot persists everything.
3. Models: pydantic v2 models for everything crossing the wire, matching Section 13
   verbatim; snake_case JSON throughout.
4. HTTP: httpx for all outbound calls; async endpoints; type hints everywhere; routers
   stay thin (logic in extraction/, agent/, calculator.py).
5. AI boundaries (Sections 14, 24): Gemini (gemini-1.5-flash, temperature 0) used ONLY
   for document field extraction and agent reasoning — never for math or distances;
   missing fields are null, never guessed; calculator.py is 100% deterministic.
6. Agent safety (Section 16): tools limited to the three read-only marketplace GETs +
   the local simulate_payment; no writes, no other endpoints, ever.
7. Error handling: HTTPException with correct codes (422 unprocessable document, 502
   upstream Gemini failure); one retry on transient Gemini failures, then fail loudly
   (extraction) or NO_PURCHASE (agent).
8. Logging: request path + outcome logged; presigned/document URLs truncated at the
   query string; GEMINI_API_KEY never logged.
9. API contracts: request/response shapes match COMMANDO.md Section 8.11 exactly.
10. Edge cases: the failure paths listed for this step are actually handled.
11. Scope discipline: NO future-step functionality, no placeholder/TODO code, every
    function fully implemented. Production code never imports from mocks/ — only which
    server answers at SPRING_BOOT_URL / marketplace_base_url decides mock vs real.
12. Startup: uvicorn main:app starts cleanly.

{STEP_EXTRA_CHECKS}
```

### 0.4 Standard Testing Prompt (template)

COMMANDO.md Section 22 mandates a **manual testing policy** (curl/Postman + DB inspection); no unit-test framework is required. Testing prompts therefore ask Claude Code to produce executable test scripts and checklists, not JUnit suites.

```
For {STEP_ID} ONLY, create the test assets below. Do not add test frameworks or
dependencies. Do not modify application code. Follow COMMANDO.md Section 22.

1. Create/extend a file `testing/track-{a|b|c}/step-{STEP_ID}-tests.sh` (shared I/F
   steps use `testing/shared/step-{STEP_ID}-tests.sh`) containing curl commands that
   exercise EVERY endpoint or behavior added in this step:
   - Happy path with the exact request bodies from COMMANDO.md Section 8.
   - Negative cases: {NEGATIVE_CASES}.
   - Auth cases where relevant: missing token (401), wrong role (403).
   Each command must have a comment stating the expected HTTP status and the expected
   ApiResponse shape.
2. Create/extend `testing/track-{a|b|c}/step-{STEP_ID}-db-checks.sql` with SELECT
   statements to verify the expected database state after the script runs (Track A only —
   Tracks B and C have no database; substitute assertions on mock/server logs or on
   response bodies).
3. Add a short `testing/track-{a|b|c}/step-{STEP_ID}-README.md` describing run order,
   prerequisites (which services AND which mocks must be running), and what "pass" looks
   like, including expected log lines.
4. Where external systems are involved (S3, Gemini, SMTP, FastAPI), state the mocking/
   substitution strategy: {MOCKING_STRATEGY}.
Coverage expectation: every new endpoint has at least one success and one failure test;
every business rule from COMMANDO.md Section 9 touched by this step has a test.
```

### 0.5 Fix Prompt (from COMMANDO.md Section 22 — use whenever anything fails)

```
The project fails with this error: [paste full error]
Fix this error without modifying any other working code.
Only change what is necessary to resolve this specific error.
Compile and verify the fix works.
```

### 0.6 Repository Layout Established by This Guide

```
carbontrace/
├── COMMANDO.md
├── .gitignore            (.env, ai-service/.env, frontend/.env, application-local.yml)
├── .env.example
├── backend/               (Spring Boot — Track A)
├── frontend/              (React + Vite — Track B)
├── ai-service/            (FastAPI — Track C)
├── mocks/                 (COMMANDO.md Section 25.3 mocks: mock-backend/, mock-ai/,
│                           mock-marketplace/, sample-pdfs/ — each labeled
│                           "MOCK — replaced in <swap-step-id>", each with a README,
│                           each DELETED by its swap step; never imported by
│                           production code)
└── testing/
    ├── track-a/           (Track A curl scripts + SQL checks per step)
    ├── track-b/           (Track B test assets per step)
    ├── track-c/           (Track C test assets per step)
    └── shared/            (I/F-step scripts and reports)
```

### 0.7 Mock Creation Prompt (template)

Used by the mock-build steps (A020, B002, C002). Fill `{MOCK_NAME}`, `{STANDS_IN_FOR}`, `{ENDPOINTS}`, `{PORT}`, `{TOOLING}`, `{SWAP_STEP}`; the step may append mock-specific behavioral requirements.

```
Read COMMANDO.md Sections 8 and 25 and implement the mock {MOCK_NAME} ONLY.
Create it under mocks/{MOCK_NAME}/ — it stands in for {STANDS_IN_FOR}.
Rules (COMMANDO.md Sections 25.2–25.4 — non-negotiable):
1. Serve {ENDPOINTS} on port {PORT} using {TOOLING} — Section 25.3 tooling only; no
   Docker, no test frameworks, nothing outside the agreed stack.
2. Every response body is built STRICTLY from the COMMANDO.md Section 8 examples: same
   field names, same casing, same ApiResponse<T>/PagedResponse wrapper, same status
   codes, same example values. Do NOT invent fields, rename keys, alter shapes, or
   "improve" anything. Where Section 8 shows no example value for a field, derive one
   from the closest Section 8 example — never from imagination.
3. Include the error cases the consuming steps need (401/400/403/404 and any others
   listed by the build step), each in the exact ApiResponse.error shape.
4. Add mocks/{MOCK_NAME}/README.md stating: how to run it, the port, what it stands in
   for, and the swap step that retires it ({SWAP_STEP}). Put the label
   "MOCK — replaced in {SWAP_STEP}" at the top of the README and as a comment at the
   top of every source file.
5. The mock must NEVER be imported by production code and must not depend on any
   track's codebase.
No placeholders, no TODOs. Verify it starts and every endpoint answers with the exact
Section 8 shape (curl each one and diff against the spec examples).
```

### 0.8 Integration Swap Prompt (template)

Used by every swap step (A-SWAP-1/2/3, B-SWAP-1, C-SWAP-1). Fill `{SWAP_ID}`, `{MOCK}`, `{REAL_DEPENDENCY}`, `{CONFIG_CHANGE}`, `{ORIGINAL_TESTS}`.

```
Read COMMANDO.md (especially Sections 8 and 25), the mock's code and README, and the
real dependency's endpoints, then execute {SWAP_ID} ONLY.
Precondition: the providing track has ticked the corresponding row in the Integration
Readiness Checklist (Shared Integration phase). This step replaces {MOCK} with
{REAL_DEPENDENCY}.
1. Read both sides first so you know exactly what is being swapped.
2. Switch configuration/URLs from mock to real: {CONFIG_CHANGE}. Configuration ONLY —
   change NO business logic, no DTOs, no shapes, no handlers.
3. Re-run the original step test scripts ({ORIGINAL_TESTS}) against the REAL dependency
   and diff the observed behavior against the recorded mock behavior field by field.
4. Delete {MOCK} — the whole directory, or only the specific mock route if other routes
   are still awaiting their own swap steps — and its README entry. A mock outliving its
   swap step is a defect (COMMANDO.md Section 25.4).
5. If ANY request/response disagrees with COMMANDO.md Section 8, report it as a
   Section 8 discrepancy naming the violating side — NEVER silently adapt code or mock
   to the difference. Contract changes go to COMMANDO.md Section 8 FIRST (Section 25.2).
List every file touched and every behavioral diff observed.
```

### 0.9 Contract Consistency Check Prompt (template)

Every member runs this at the END of every phase and BEFORE every swap step. Fill `{TRACK}`.

```
Read COMMANDO.md Section 8 (including 8.11) end to end. Then audit ALL of {TRACK}'s
code AND every mock it owns under mocks/ for contract drift against Section 8:
- field names and casing (camelCase Spring Boot / snake_case FastAPI),
- the ApiResponse<T> wrapper and the PagedResponse pagination shape,
- HTTP status codes per endpoint,
- enum values (transport modes, fuel types, statuses, confidence levels, verification
  standards, project types, decisions),
- pinned example values where the contract fixes them.
Report PASS/FAIL per endpoint with file/line references. Fix ONLY genuine drift (code
or mock disagreeing with Section 8); change nothing that already complies. If you
believe Section 8 itself is wrong, STOP and report it — do not "fix" the contract in
code or mocks (COMMANDO.md Section 25.2).
```

### 0.10 Cross-Track Error Triage Prompt (template)

Use whenever an integration or swap-step failure spans two tracks. Fill `{FAILURE}`.

```
Read COMMANDO.md Section 8 and diagnose this cross-track failure: {FAILURE}
[paste the failing request, the response, and both sides' relevant logs].
1. Determine which side violates COMMANDO.md Section 8 — the contract is the ONLY
   arbiter. Quote the exact Section 8 lines that decide it.
2. Fix ONLY the violating side, with a minimal diff. Do not touch the compliant side.
3. If BOTH sides comply and the failure is environmental (ports, CORS, env vars,
   processes not running), fix the environment and say so.
4. If the contract itself is ambiguous or wrong, STOP: never resolve a mismatch by
   changing the contract in code — COMMANDO.md Section 8 must be updated FIRST, agreed
   by all three members, and only then propagated to code and mocks (Section 25.2).
Report: violating side, root cause, fix applied, and the re-run result.
```

---

# ═══ TRACK A — BACKEND + AWS (Developer A starts here on day one) ═══

Track A owns `backend/`, ALL AWS/Gmail manual setups (A, B, C), and `testing/track-a/`. It builds `mocks/mock-ai/` (STEP A020) to stand in for the FastAPI service until the A-SWAP steps retire it route by route. Work on branch `track-a`; verification prompts use template 0.3. Nothing in Phases A1–A5 depends on Track B or Track C.

# PHASE A1 — PROJECT FOUNDATION (STEPS A001–A006)

**Purpose:** Stand up the Spring Boot skeleton, configuration, RDS connectivity, shared response classes, global error handling, and the security scaffolding everything else plugs into.

**Prerequisites:** Java 17 JDK, Maven 3.9+, Git installed. An AWS account exists (RDS is created in MANUAL SETUP A before STEP A003). No API keys needed yet except AWS/RDS.

**Deliverables:** A compiling, bootable Spring Boot 3.3.x app on port 8080 connected to RDS PostgreSQL 16, with CORS, S3/AI config beans, `ApiResponse`/`PagedResponse`/`AppConstants`, `GlobalExceptionHandler`, and a JWT security chain (no auth endpoints yet).

**Files/Folders expected:** `backend/pom.xml`, `backend/src/main/resources/application.yml`, packages `com.carbontrace.{config,security,common,exception,modules}` exactly per COMMANDO.md Section 6.

**Dependencies on previous phases:** None (first phase).

**Phase completion checklist:**
- [ ] `mvn compile` and `mvn spring-boot:run` succeed
- [ ] App connects to RDS (Hibernate logs show dialect + connection)
- [ ] All config beans load (S3Client, S3Presigner, RestTemplate)
- [ ] Unknown route returns the ApiResponse 401/404 JSON shape (security chain active)
- [ ] Nothing beyond Phase A1 scope exists in the codebase
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### STEP A001 — Spring Boot Project Initialization

**Objective:** Create the Maven project with all dependencies and the empty package skeleton from COMMANDO.md Section 6.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A001 ONLY.
Create the Spring Boot project in ./backend:
- Maven, Java 17, Spring Boot 3.3.x (latest stable compatible with Java 17), groupId
  com.carbontrace, artifactId carbontrace-backend.
- Dependencies EXACTLY per COMMANDO.md Section 4 backend stack: spring-boot-starter-web,
  spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation,
  spring-boot-starter-mail, postgresql driver, lombok, mapstruct 1.5.5.Final (+ annotation
  processor configured alongside lombok in maven-compiler-plugin), jjwt-api/impl/jackson
  0.12.x, AWS SDK v2 s3 + s3-presigner ONLY.
- Create CarbonTraceApplication.java and the EMPTY package skeleton exactly as COMMANDO.md
  Section 6 (config, security, common, exception, modules.* subpackages) — package-info or
  empty directories only, no classes yet beyond the main class.
- Create the repo-root .gitignore covering .env, ai-service/.env, frontend/.env,
  application-local.yml, target/, node_modules/.
- Create .env.example at repo root copied verbatim from COMMANDO.md Section 19.
Do NOT create application.yml, config classes, entities, or any future-step files.
Do NOT introduce placeholders or TODOs. Verify with mvn -f backend/pom.xml compile.
```

**Expected Output:**
- Created: `backend/pom.xml`, `backend/src/main/java/com/carbontrace/CarbonTraceApplication.java`, package skeleton, `.gitignore`, `.env.example`.
- No endpoints, no tables, no configs yet.

**Verification Prompt:** Use template 0.3 with `{STEP_ID}=STEP A001`, `{STEP_SCOPE}=Maven project init, dependency list, package skeleton`, `{STEP_EXTRA_CHECKS}=`
```
13. pom.xml contains ONLY the dependencies listed in COMMANDO.md Section 4 (no Gradle files,
    no actuator/devtools/testcontainers extras); Java 17 enforced; MapStruct + Lombok
    annotation-processor ordering is correct (lombok-mapstruct-binding present).
14. Package tree matches Section 6 exactly, including modules.* subpackages.
```

**Testing Prompt:** Use template 0.4 with `{STEP_ID}=A001`, `{NEGATIVE_CASES}=none (no endpoints yet — script only runs mvn compile and asserts exit code 0)`, `{MOCKING_STRATEGY}=none required`.

**Manual Testing:**
- Run: `cd backend && mvn compile` → `BUILD SUCCESS`.
- Run (skip `git init` — the shared team repo from COMMANDO.md Section 25.1 already exists; work on branch `track-a`): `git add -A && git status` → `.env.example` tracked; no `target/`.
- Possible failures: annotation-processor ordering (MapStruct before Lombok) breaks builds later — confirm `maven-compiler-plugin` has `lombok`, `lombok-mapstruct-binding`, `mapstruct-processor` in `annotationProcessorPaths`.

**Success Criteria:**
- [ ] `mvn compile` succeeds
- [ ] Package skeleton matches Section 6 exactly
- [ ] Only Section 4 dependencies present
- [ ] `.gitignore` + `.env.example` created

**Next Step:** STEP A002.

---

### STEP A002 — Application Configuration

**Objective:** Add `application.yml` and the four config classes (AppConfig, CorsConfig, S3Config, AiConfig).

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A002 ONLY.
1. Create backend/src/main/resources/application.yml copied VERBATIM from COMMANDO.md
   Section 19 (env-var placeholders with the exact defaults shown).
2. In com.carbontrace.config create:
   - AppConfig: a RestTemplate @Bean. Configure it with a SimpleClientHttpRequestFactory
     using connect timeout 5s and read timeout 120s (the maximum any FastAPI call needs
     per COMMANDO.md Section 18).
   - CorsConfig: exactly the policy in COMMANDO.md Section 10 (origin http://localhost:5173,
     methods GET/POST/PUT/DELETE/OPTIONS, headers Authorization + Content-Type,
     credentials true).
   - S3Config: S3Client and S3Presigner beans exactly as shown in COMMANDO.md Section 11,
     region from app.aws.region, default credential chain, NO hardcoded credentials.
   - AiConfig: @Getter config exposing app.fastapi.base-url and
     app.marketplace.public-base-url exactly as in Section 11.
Modify only these files. Do not create security, entities, or MailConfig (mail starter
auto-config via properties is sufficient per Section 6). No placeholders.
Verify mvn compile succeeds.
```

**Expected Output:**
- Created: `application.yml`, `AppConfig.java`, `CorsConfig.java`, `S3Config.java`, `AiConfig.java`.
- Beans: RestTemplate, S3Client, S3Presigner. No endpoints yet.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A002`, `{STEP_SCOPE}=application.yml + config beans`, extra checks:
```
13. application.yml matches COMMANDO.md Section 19 verbatim (every key, every default).
14. No AWS keys, DB passwords, or mail passwords appear anywhere in code or yml literals.
15. CORS matches Section 10 exactly; RestTemplate timeouts respect Section 18.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A002`, `{NEGATIVE_CASES}=app fails fast with a clear message if DB_URL is unreachable (documented, not yet run)`, `{MOCKING_STRATEGY}=none — startup test deferred to STEP A003 when RDS exists`.

**Manual Testing:**
- `mvn compile` → SUCCESS. Do NOT run the app yet (no DB).
- Inspect yml against Section 19 line by line.

**Success Criteria:**
- [ ] Compiles; yml verbatim from spec; 4 config classes; no secrets in code

**Next Step:** MANUAL SETUP A, then STEP A003.

---

### 🔧 MANUAL SETUP A — PostgreSQL 16 (do this YOURSELF)

Per COMMANDO.md Section 17 "Database Environment Policy", the database is
**local-first**: development and ALL testing run against a local PostgreSQL 16,
and the project switches to Amazon RDS as the first task of the deployment
phase. Part A.1 is required now; Part A.2 is deferred.

---

#### A.1 — Local PostgreSQL 16 (REQUIRED before STEP A003, ~10 min)

**Sub-steps:**
1. Install **PostgreSQL 16** (must be 16 — matches the RDS target and COMMANDO.md Section 4).
   - Windows: `winget install --id PostgreSQL.PostgreSQL.16 --exact`
   - macOS: `brew install postgresql@16 && brew services start postgresql@16`
   - Linux: use your distro's `postgresql-16` package.
2. During installation set the **superuser (`postgres`) password to `postgres`**, port **5432**.
   These are exactly the COMMANDO.md Section 19 defaults, so the application runs
   with **no environment variables set at all**. A weak password is acceptable
   here and ONLY here: the server listens on localhost and is never exposed.
3. Create the database:
   `psql -U postgres -c "CREATE DATABASE carbontrace;"`
4. Confirm the service starts automatically with your machine.

**Verification of manual work:**
- [ ] `psql -U postgres -d carbontrace -c "SELECT version();"` returns **PostgreSQL 16.x**.
- [ ] `\l` lists a `carbontrace` database.
- [ ] `\dt` reports no relations yet (no entities exist until STEP A007).
- [ ] No password is written into any tracked file (`git status` clean of secrets).

> Why local-first: an RDS instance with `Public access = Yes` must allow your
> machine's public IP, and home/mobile IPs rotate — silently breaking every new
> connection while already-established ones keep working. That failure mode
> costs more time than it saves. Local PostgreSQL removes it entirely.

---

#### A.2 — Amazon RDS PostgreSQL 16 (DEFERRED — do before deployment)

Not required by any build step. Do it when you reach the deployment phase (see
the FINAL PROMPT at the end of this guide), or earlier if you want to prove the
AWS path. Nothing blocks on it.

**Sub-steps:**
1. Sign in to the AWS Console → RDS → **Create database**.
2. Choose **Standard create** (Easy create CANNOT produce a publicly accessible instance).
3. Engine options → **PostgreSQL** — NOT *Aurora (PostgreSQL Compatible)*, which is a different engine and is not free-tier eligible. Version **16.x** (the dropdown defaults to the newest major; change it).
4. Template: **Free tier**. Accounts created after 15 Jul 2025 show **Sandbox** instead — free usage is now credit-based, so pick Sandbox or Dev/Test.
5. Settings: DB instance identifier `carbontrace-db`; Credentials Settings → **Self managed**; master username `postgres`; set a strong master password and store it in a password manager.
6. Instance configuration: `db.t4g.micro`, or `db.t3.micro` if you hit `insufficient-capacity`.
7. Connectivity: **Don't connect to an EC2 compute resource** (choosing the EC2 option forces `Public access = No`). **Public access = Yes** (development only). VPC default. Create a new security group `carbontrace-dev-sg`.
8. Additional configuration → **Initial database name: `carbontrace`** (easily missed; skipping it means the database is never created). Disable automated backups if you want (demo).
9. Create database; wait until status = **Available** (~10 min).
10. Edit the security group `carbontrace-dev-sg` → Inbound rules → add rule: Type **PostgreSQL (5432)**, Source **My IP** ONLY. Remove any 0.0.0.0/0 rule.
11. Copy the **Endpoint** hostname from the RDS instance page.
12. Set the values in your shell / IDE run config / git-ignored `application-local.yml`:
    `DB_URL=jdbc:postgresql://<endpoint>:5432/carbontrace?sslmode=require`, `DB_USERNAME=postgres`, `DB_PASSWORD=<your password>`.
    **`?sslmode=require` is mandatory** — RDS PostgreSQL 15+ sets `rds.force_ssl=1` and rejects unencrypted connections.

**Verification of manual work (when you do it):**
- [ ] From your machine: `psql "host=<endpoint> port=5432 dbname=carbontrace user=postgres sslmode=require"` connects (or Beekeeper Studio connects). If it hangs: your IP rule or Public access is wrong — public IPs rotate, so re-set Source = My IP.
- [ ] `SELECT version();` returns PostgreSQL 16.x.
- [ ] Password is NOT written into any tracked file (`git status` clean of secrets).

---

### STEP A003 — Database Connection and Startup Verification

**Objective:** Boot the application against the **local PostgreSQL 16** from MANUAL SETUP A.1 and confirm Hibernate connectivity. (Per COMMANDO.md Section 17 "Database Environment Policy", RDS is the pre-deployment target, not the development database. If you have already provisioned RDS, you may verify against it too — the step is identical, only `DB_URL` differs.)

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A003 ONLY.
The RDS instance exists and env vars DB_URL, DB_USERNAME, DB_PASSWORD are set.
1. If the RDS instance enforces SSL, ensure the JDBC URL supports appending
   ?sslmode=require (document this in a new backend/README-dev.md note; do not
   hardcode credentials).
2. Temporarily verify startup: run mvn spring-boot:run with the env vars and confirm
   HikariCP connects and the app starts on port 8080. Fix ONLY connection-related
   configuration issues if startup fails.
3. Do not create entities, endpoints, or tables. Do not change application.yml defaults
   except where a genuine connection bug exists.
```

**Expected Output:**
- Modified (possibly): none or `application.yml` sslmode note; Created: `backend/README-dev.md` (run instructions).
- Running app on :8080 connected to RDS. No tables yet (no entities).

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A003`, `{STEP_SCOPE}=DB connectivity`, extra:
```
13. No credentials committed; README-dev.md explains env vars without containing values.
14. ddl-auto is still 'update' and show-sql true per Section 19.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A003`, `{NEGATIVE_CASES}=wrong password → HikariCP fails with auth error (document expected message); unreachable host → timeout`, `{MOCKING_STRATEGY}=none — real RDS`.

**Manual Testing:**
- `cd backend && mvn spring-boot:run` → logs show `HikariPool-1 - Start completed` and `Tomcat started on port 8080`.
- In psql: `\dt` → no tables yet (expected).
- Possible failures: security-group IP changed (home IP rotates) → re-edit inbound rule.

**Success Criteria:**
- [ ] App boots against RDS; no secrets committed; README-dev.md exists

**Next Step:** STEP A004.

---

### STEP A004 — Common Classes

**Objective:** Create `ApiResponse`, `PagedResponse`, and `AppConstants` used by every later module.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A004 ONLY.
In com.carbontrace.common create ApiResponse.java, PagedResponse.java, and
AppConstants.java copied VERBATIM from COMMANDO.md Section 11, including:
- ApiResponse static success()/error() factories.
- PagedResponse fields exactly as listed.
- AppConstants: private constructor, String page defaults (they are @RequestParam
  defaultValue values), role constants, auth header constants, OTP constants,
  upload/presign constants — all exactly as Section 11.
Modify nothing else. No extra utility classes. Verify mvn compile.
```

**Expected Output:** Created: 3 classes in `common/`. Nothing else.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A004`, `{STEP_SCOPE}=common classes`, extra:
```
13. Classes match Section 11 verbatim (constant names, types, values; String page defaults).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A004`, `{NEGATIVE_CASES}=none`, `{MOCKING_STRATEGY}=none — compile-only step`.

**Manual Testing:** `mvn compile` → SUCCESS.

**Success Criteria:** [ ] Compiles; classes verbatim; no extras.

**Next Step:** STEP A005.

---

### STEP A005 — Global Exception Handling

**Objective:** Create the custom exceptions and `GlobalExceptionHandler` mapping every error to the ApiResponse format.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A005 ONLY.
In com.carbontrace.exception create:
- ResourceNotFoundException, BadRequestException, UnauthorizedException
  (RuntimeExceptions with message constructors).
- GlobalExceptionHandler (@RestControllerAdvice) implementing EVERY mapping in
  COMMANDO.md Section 23: 404, 400, 401, 403 (AccessDeniedException),
  400 for MethodArgumentNotValidException using the FIRST field error message,
  409 for DataIntegrityViolationException, 502 for ResourceAccessException/
  RestClientException with message "AI service unavailable — please try again",
  500 catch-all. Every response body is ApiResponse.error(message).
Do NOT create ShipmentException or PurchaseException yet (they belong to their
modules in later steps); the handler must be easy to extend. Log every handled
exception with @Slf4j (stack trace at ERROR for 500/502 only). Verify mvn compile.
```

**Expected Output:** Created: 3 exceptions + `GlobalExceptionHandler`. No endpoints.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A005`, `{STEP_SCOPE}=exception layer`, extra:
```
13. Status-code table matches Section 23 exactly; all bodies use ApiResponse.error.
14. Validation errors surface the first field error message only.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A005`, `{NEGATIVE_CASES}=none runnable yet — script documents the mappings and defers HTTP assertions to STEP A012`, `{MOCKING_STRATEGY}=none`.

**Manual Testing:** `mvn compile`; app still boots.

**Success Criteria:** [ ] Compiles; all Section 23 mappings present; module exceptions NOT created early.

**Next Step:** STEP A006.

---

### STEP A006 — Security Scaffolding

**Objective:** Add the JWT infrastructure and security filter chain (auth endpoints come in Phase A2).

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A006 ONLY.
In com.carbontrace.security create:
- JwtTokenProvider: JJWT 0.12.x, HS256, secret/expiration from app.jwt.* properties;
  generates tokens with claims exactly per COMMANDO.md Section 10 (sub=email, userId,
  role, iat, exp); validates and parses tokens; never logs token contents.
- JwtAuthenticationFilter (OncePerRequestFilter): reads the Authorization header using
  AppConstants.AUTH_HEADER/TOKEN_PREFIX, validates, populates SecurityContext.
- AuthEntryPoint: returns 401 with ApiResponse.error JSON.
- CustomUserDetailsService: STUB-FREE requirement conflict note — the User entity does
  not exist until STEP A007, so in THIS step create only the class skeleton that
  Spring Security requires wired to a UserRepository is NOT possible. Therefore:
  implement SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, AuthEntryPoint
  fully NOW, and defer CustomUserDetailsService creation to STEP A009 (COMMANDO.md
  Section 20 places it there). Do not create an empty CustomUserDetailsService.
- SecurityConfig: stateless session, CSRF off, the exact permit/role matrix from
  COMMANDO.md Section 10: permitAll /api/auth/** and GET /api/marketplace/**;
  ROLE_ADMIN for /api/admin/**, POST/PUT /api/emission-factors/**, PUT /api/vendors/{id}
  and toggle-active; everything else authenticated. Register the JWT filter and
  AuthEntryPoint. Expose PasswordEncoder (BCrypt) and AuthenticationManager beans.
App must still boot. Verify mvn compile and startup.
```

**Expected Output:**
- Created: `JwtTokenProvider`, `JwtAuthenticationFilter`, `AuthEntryPoint`, `SecurityConfig`.
- NOT created yet: `CustomUserDetailsService` (STEP A009 per spec order).
- Behavior: every non-permitted route now returns 401 ApiResponse JSON.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A006`, `{STEP_SCOPE}=JWT + filter chain`, extra:
```
13. Token claims match Section 10's JWT structure; HS256; expiries from properties.
14. Filter-chain matrix matches Section 10 exactly, including GET-only marketplace permit.
15. No JWT or secret values logged anywhere.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A006`, `{NEGATIVE_CASES}=GET /api/anything without token → 401 ApiResponse JSON; malformed Bearer token → 401`, `{MOCKING_STRATEGY}=none — hit the running app`.

**Manual Testing:**
- Boot app. `curl -i http://localhost:8080/api/anything` → `401` with `{"success":false,...}`.
- `curl -i http://localhost:8080/api/marketplace/credits` → NOT 401 (404 acceptable — endpoint doesn't exist yet, but the chain permits it).

**Success Criteria:**
- [ ] Boots; 401 JSON on protected routes; marketplace GET permitted; Phase A1 checklist all green.

**Next Step:** PHASE A2 — MANUAL SETUP B, then STEP A007.

---

# PHASE A2 — AUTHENTICATION WITH OTP (STEPS A007–A012)

**Purpose:** Full auth: register → email OTP → verify → login/refresh-rotation → forgot/reset password.

**Prerequisites:** Phase A1 complete. Gmail app password (MANUAL SETUP B) — OR use the console-log OTP fallback and do Manual Setup B later, before STEP I002.

**Deliverables:** `users`, `otp_codes`, `refresh_tokens` tables in RDS; all 8 auth endpoints per COMMANDO.md Section 8.1; working OTP delivery (SMTP or console fallback).

**Files/Folders:** everything under `modules/auth/**`, `modules/user` deferred to Phase A3, `security/CustomUserDetailsService.java`.

**Dependencies:** Phase A1 (security scaffolding, common classes, exception handler, RDS).

**Phase completion checklist:**
- [ ] Register → OTP → verify → auto-login tokens works end to end
- [ ] Unverified login rejected with "Email not verified"
- [ ] OTP expiry (10 min), single-use, 5-attempt limit, resend invalidation all enforced
- [ ] Refresh rotation revokes old token
- [ ] forgot-password returns generic 200 for unknown emails
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### 🔧 MANUAL SETUP B — Gmail App Password (do this YOURSELF, ~5 min; can defer)

Needed for real OTP emails from STEP A010. Until done, set `LOG_OTP_FALLBACK=true` (default) and read OTPs from the console.

**Sub-steps:**
1. Google Account → Security → enable **2-Step Verification** (required for app passwords).
2. Security → **App passwords** → app name `carbontrace` → Create.
3. Copy the 16-character password (spaces don't matter).
4. Set env vars: `MAIL_USERNAME=youraddress@gmail.com`, `MAIL_APP_PASSWORD=<16 chars>`.

**Verification of manual work:**
- [ ] After STEP A010, registering with a real email delivers the OTP mail (check spam).
- [ ] The app password is only in env/.env (gitignored), never in yml literals.
- [ ] If skipped: app logs `OTP for <email>: <code>` and registration still succeeds.

---

### STEP A007 — Auth Entities and Repositories

**Objective:** Create `User`, `RefreshToken`, `OtpCode` entities + their repositories so Hibernate creates the first three tables.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A007 ONLY.
In modules/auth/entity create User, RefreshToken, OtpCode JPA entities whose generated
DDL matches COMMANDO.md Section 7 tables users, refresh_tokens, otp_codes EXACTLY
(column names, lengths, defaults, unique constraints, the idx_otp_email_purpose index
via @Table(indexes=...)). Rules: Lombok @Data/@NoArgsConstructor/@AllArgsConstructor/
@Builder; @Enumerated(EnumType.STRING) for role and otp purpose enums (define Role and
OtpPurpose enums in the entity package); timestamps via @CreationTimestamp/
@UpdateTimestamp; RefreshToken has @ManyToOne User.
In modules/auth/repository create UserRepository (findByEmail, existsByEmail),
RefreshTokenRepository (findByToken, deleteByUser or revocation query),
OtpCodeRepository (find latest unused by email+purpose, bulk mark-used update).
No services, no controllers, no DTOs yet. Boot the app and confirm Hibernate creates
the 3 tables in RDS. Verify mvn compile.
```

**Expected Output:**
- Created: 3 entities + 2 enums + 3 repositories.
- DB tables created: `users`, `refresh_tokens`, `otp_codes` (+ index).

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A007`, `{STEP_SCOPE}=auth entities/repositories`, extra:
```
13. Generated DDL matches Section 7 exactly — verify column-by-column via \d users in psql.
14. Repository methods return Optional where single-row; no extra tables created.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A007`, `{NEGATIVE_CASES}=duplicate email insert violates unique constraint (SQL check)`, `{MOCKING_STRATEGY}=none — verify via psql`.

**Manual Testing:**
- Boot app; in psql: `\dt` → 3 tables; `\d users` matches Section 7; `\d otp_codes` shows the index.

**Success Criteria:** [ ] 3 tables exact match; app boots; no extra code.

**Next Step:** STEP A008.

---

### STEP A008 — Auth DTOs

**Objective:** Create all eight auth DTOs with Jakarta validation mirroring Section 9 rules.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A008 ONLY.
In modules/auth/dto create exactly these classes from COMMANDO.md Section 6:
RegisterRequest, LoginRequest, VerifyOtpRequest, ResendOtpRequest,
ForgotPasswordRequest, ResetPasswordRequest, RefreshTokenRequest, AuthResponse.
Validation per Section 9: email @Email @NotBlank; password @NotBlank @Size(min=8) plus
a @Pattern enforcing at least one uppercase, one lowercase, one digit; role must be
ROLE_AUDITOR or ROLE_ADMIN (@Pattern); OTP code @Pattern 6 digits; purpose must be
REGISTRATION or PASSWORD_RESET. AuthResponse fields exactly match the Section 8.1
verify-otp/login response payload (accessToken, refreshToken, tokenType, userId, email,
role, firstName, lastName). Lombok on all. No services/controllers. mvn compile.
```

**Expected Output:** Created: 8 DTOs. Nothing else.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A008`, `{STEP_SCOPE}=auth DTOs`, extra:
```
13. Field names match Section 8.1 JSON keys (camelCase) exactly.
14. Password regex enforces all three character classes; role/purpose constrained.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A008`, `{NEGATIVE_CASES}=documented invalid payloads (short password, bad role, 5-digit OTP) for use in STEP A012's script`, `{MOCKING_STRATEGY}=none`.

**Manual Testing:** `mvn compile`.

**Success Criteria:** [ ] 8 DTOs, correct validation, compiles.

**Next Step:** STEP A009.

---

### STEP A009 — CustomUserDetailsService

**Objective:** Wire Spring Security user loading to the `users` table.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A009 ONLY.
In com.carbontrace.security create CustomUserDetailsService implementing
UserDetailsService: load by email via UserRepository, map role to a single
GrantedAuthority, map is_active to enabled/locked semantics (inactive users cannot
authenticate), throw UsernameNotFoundException when absent. Constructor injection via
@RequiredArgsConstructor. Ensure SecurityConfig/AuthenticationManager picks it up.
App must boot. mvn compile.
```

**Expected Output:** Created: `CustomUserDetailsService`. Modified (if needed): `SecurityConfig` wiring.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A009`, `{STEP_SCOPE}=UserDetailsService`, extra:
```
13. Inactive users are blocked from authentication; verified flag NOT checked here (it is
    an AuthService login rule per Section 9, returning a clear 400).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A009`, `{NEGATIVE_CASES}=unknown email lookup path documented`, `{MOCKING_STRATEGY}=none — behavior exercised via STEP A012 endpoints`.

**Manual Testing:** App boots; no behavior change observable yet.

**Success Criteria:** [ ] Compiles/boots; correct role mapping and active-flag handling.

**Next Step:** STEP A010.

---

### STEP A010 — EmailService and OtpService

**Objective:** Implement OTP generation/verification with expiry, attempts, single-use, resend invalidation, and Gmail SMTP delivery with console fallback.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A010 ONLY.
In modules/auth create service interfaces EmailService and OtpService with impls in
serviceimpl, per the Section 11 pattern.
OtpServiceImpl rules (COMMANDO.md Section 9):
- generateOtp(email, purpose): random 6-digit numeric code (SecureRandom), expiry 10
  minutes (AppConstants), before saving mark ALL previous unused OTPs for (email,
  purpose) as used, persist new row, return the code.
- verifyOtp(email, code, purpose): load latest unused, unexpired row; increment attempts
  on wrong code and invalidate after 5 attempts (AppConstants.OTP_MAX_ATTEMPTS); on
  success set is_used=true. Throw BadRequestException with precise messages for
  expired / invalid / attempt-limit cases.
EmailServiceImpl:
- sendOtpEmail(email, code, purpose): simple HTML mail (code, purpose, 10-minute note)
  via JavaMailSender (spring.mail auto-config).
- If app.mail.log-otp-fallback=true OR sending throws, log "OTP for {email}: {code}" at
  WARN and do not fail the calling flow (per Section 18 Flow 8). Exception: the OTP code
  may be logged ONLY through this explicit fallback path — nowhere else.
No controllers yet. mvn compile + boot.
```

**Expected Output:** Created: `EmailService`, `OtpService`, `EmailServiceImpl`, `OtpServiceImpl`.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A010`, `{STEP_SCOPE}=OTP + email services`, extra:
```
13. All five OTP rules from Section 9 enforced (uniqueness of active code via resend
    invalidation, expiry, single-use, attempt limit, 6-digit numeric).
14. Mail failure does not break registration; fallback logging is the only OTP log.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A010`, `{NEGATIVE_CASES}=expired OTP, wrong code x5 then correct code (must fail), reused OTP`, `{MOCKING_STRATEGY}=run with LOG_OTP_FALLBACK=true so SMTP is not required; real SMTP verified after MANUAL SETUP B`.

**Manual Testing:** Behavior exercised via STEP A012 endpoints; for now compile + boot.

**Success Criteria:** [ ] All OTP rules implemented; fallback works; compiles.

**Next Step:** STEP A011.

---

### STEP A011 — AuthService Implementation

**Objective:** Implement register, verify-otp (auto-login), resend-otp, login, refresh with rotation, forgot-password, reset-password.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A011 ONLY.
Create modules/auth AuthService interface + AuthServiceImpl covering all seven flows
per COMMANDO.md Sections 8.1 and 9:
- register: unique email check (409/400 per handler), BCrypt password, is_email_verified
  false, generate+send REGISTRATION OTP; return the Section 8.1 register data payload.
- verifyOtp: OtpService.verify; on REGISTRATION success set is_email_verified=true and
  return full AuthResponse (access + refresh tokens) — auto-login.
- resendOtp: re-issue for purpose (invalidates old ones via OtpService).
- login: authenticate via AuthenticationManager; if user not verified, throw
  BadRequestException("Email not verified"); return AuthResponse; wrong credentials → 401.
- refresh: validate stored refresh token (exists, not revoked, not expired), REVOKE it,
  issue new access + refresh tokens (rotation), persist new row.
- forgotPassword: if email exists, issue PASSWORD_RESET OTP; ALWAYS return the same
  generic success message (no account enumeration).
- resetPassword: verify OTP purpose PASSWORD_RESET, BCrypt-encode new password (same
  strength rules), save.
Refresh tokens: random opaque strings (UUID-based, <=500 chars), 7-day expiry from
properties. No controller yet. mvn compile + boot.
```

**Expected Output:** Created: `AuthService`, `AuthServiceImpl`. Modified: none beyond wiring.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A011`, `{STEP_SCOPE}=auth service flows`, extra:
```
13. Refresh rotation revokes the old token in the same operation.
14. forgot-password response is identical for existing and unknown emails.
15. Unverified login produces exactly the "Email not verified" 400.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A011`, `{NEGATIVE_CASES}=duplicate register, unverified login, revoked refresh reuse, expired refresh`, `{MOCKING_STRATEGY}=console OTP fallback`.

**Manual Testing:** Deferred to STEP A012 (no controller yet). Compile + boot only.

**Success Criteria:** [ ] All 7 flows implemented per spec; compiles.

**Next Step:** STEP A012.

---

### STEP A012 — AuthController + Full Auth Endpoint Pass

**Objective:** Expose all 8 auth endpoints and verify every success/failure path with curl.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A012 ONLY.
Create modules/auth/controller/AuthController with these public endpoints, request/
response shapes and status codes EXACTLY per COMMANDO.md Section 8.1:
POST /api/auth/register (201), /verify-otp, /resend-otp, /login, /refresh,
/forgot-password, /reset-password (all 200). All bodies validated with @Valid; all
responses wrapped in ApiResponse. No other endpoints, no user module. mvn compile,
boot, and confirm register→verify→login works locally with the console OTP fallback.
```

**Expected Output:**
- Created: `AuthController`. Endpoints live: 7 routes under `/api/auth`.
- DB: rows appear in `users`, `otp_codes`, `refresh_tokens`.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A012`, `{STEP_SCOPE}=auth controller`, extra:
```
13. Response messages and data payloads match Section 8.1 examples verbatim.
14. Endpoints are reachable without a token (permitAll confirmed).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A012`, `{NEGATIVE_CASES}=invalid password format (400 first-field message), duplicate email, wrong OTP 5x then correct (must stay rejected), expired OTP, unverified login, wrong password (401), refresh reuse after rotation (401/400), reset with wrong purpose OTP`, `{MOCKING_STRATEGY}=LOG_OTP_FALLBACK=true; read codes from app logs`.

**Manual Testing:**
- `bash testing/track-a/step-A012-tests.sh` end to end; sample:
  - `curl -s -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' -d '{"email":"a@b.com","password":"Password123!","firstName":"A","lastName":"B","companyName":"Acme","role":"ROLE_AUDITOR"}'` → 201, `otpExpiresInMinutes: 10`.
  - Grab OTP from logs → verify-otp → 200 with tokens.
  - login before verify → 400 "Email not verified"; after → 200.
  - refresh twice with the same token → second call rejected.
- psql: `SELECT is_used, attempts FROM otp_codes;` reflects rules.

**Success Criteria:**
- [ ] Every Section 8.1 path passes incl. all negatives; Phase A2 checklist green.

**Next Step:** PHASE A3, STEP A013.

---

# PHASE A3 — CORE MODULES (STEPS A013–A019)

**Purpose:** User profile, vendors, shipments + documents, S3 presigned upload/download, review workflow, and emission factor reference data.

**Prerequisites:** Phase A2 complete. MANUAL SETUP C (S3 + IAM) required before STEP A016.

**Deliverables:** `vendors`, `shipments`, `shipment_documents`, `emission_factors` tables; user/vendor/shipment/emission-factor endpoints per Sections 8.2–8.5; working browser/Postman direct-to-S3 upload; seed of the four GLOBAL/ANY factor rows.

**Dependencies:** Auth (JWT-protected endpoints), common classes, exception handler.

**Phase completion checklist:**
- [ ] A PDF can be PUT to S3 via presigned URL and a shipment created against it
- [ ] Review endpoint enforces status transitions (CALCULATED rejected)
- [ ] Factor fallback query returns rows in the 4-level order
- [ ] Role matrix enforced (admin-only vendor edit, factor writes)
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### STEP A013 — User Module (/api/users/me)

**Objective:** GET and PUT the current user's profile.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A013 ONLY.
Create modules/user per the Section 11 pattern: UserResponseDto, UserMapper (MapStruct),
UserService + UserServiceImpl, UserController with:
- GET /api/users/me → current user's basic info (derive user from SecurityContext/JWT).
- PUT /api/users/me → update firstName, lastName, companyName ONLY (never email, role,
  password) with validation.
Both require any authenticated role. ApiResponse wrapper. No admin endpoints (STEP A027).
mvn compile + boot.
```

**Expected Output:** Created: `modules/user/**` (dto, mapper, service, impl, controller). Endpoints: 2.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A013`, `{STEP_SCOPE}=user module`, extra:
```
13. PUT cannot alter email/role/password even if sent in the body (ignored or rejected).
14. MapStruct mapper used — no manual mapping.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A013`, `{NEGATIVE_CASES}=no token 401; attempt to change email via PUT has no effect`, `{MOCKING_STRATEGY}=real app + JWT from STEP A012 login`.

**Manual Testing:**
- Login → `curl -H "Authorization: Bearer $T" localhost:8080/api/users/me` → profile JSON.
- PUT with new firstName → 200; psql confirms update; email unchanged.

**Success Criteria:** [ ] Both endpoints per Section 8.2; immutable fields protected.

**Next Step:** STEP A014.

---

### STEP A014 — Vendor Module

**Objective:** Full vendor CRUD per Section 8.3 with role split and soft-deactivation.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A014 ONLY.
Create modules/vendor per the Section 11 pattern in the exact generation order:
Vendor entity (DDL matches Section 7 vendors table; VendorType enum LOGISTICS/
MANUFACTURING/UTILITY stored as STRING, default LOGISTICS), VendorRepository (paged
findAll + name search), VendorRequestDto (name required, optional contactEmail @Email,
country required), VendorResponseDto, VendorMapper, VendorService/Impl, VendorController:
- POST /api/vendors (AUDITOR+ADMIN, 201, always vendorType LOGISTICS in MVP)
- GET /api/vendors (any role; page, size, search params; PagedResponse)
- GET /api/vendors/{id} (any role)
- PUT /api/vendors/{id} (ADMIN only)
- PUT /api/vendors/{id}/toggle-active (ADMIN only)
Vendors are never deleted. Use AppConstants page defaults. mvn compile + boot; confirm
Hibernate creates the vendors table.
```

**Expected Output:** Created: `modules/vendor/**`. Table: `vendors`. Endpoints: 5.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A014`, `{STEP_SCOPE}=vendor module`, extra:
```
13. Admin-only routes return 403 for auditors (SecurityConfig matrix honored).
14. Pagination uses PagedResponse; search filters by name.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A014`, `{NEGATIVE_CASES}=auditor PUT /{id} → 403; missing name → 400; unknown id → 404`, `{MOCKING_STRATEGY}=two JWTs: one auditor, one admin (register+verify an ROLE_ADMIN user)`.

**Manual Testing:** Create, list w/ search, toggle-active as admin; auditor toggle → 403; psql `SELECT * FROM vendors`.

**Success Criteria:** [ ] 5 endpoints, role split, soft-delete only.

**Next Step:** STEP A015.

---

### STEP A015 — Shipment + ShipmentDocument Entities and Repositories

**Objective:** Create the two shipment entities/repositories so both tables exist before any upload logic.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A015 ONLY.
In modules/shipment create Shipment and ShipmentDocument entities matching Section 7
DDL exactly (all columns, precisions, defaults, the two indexes on shipments, unique
s3_key). Define enums stored as STRING: TransportMode (ROAD/RAIL/SEA/AIR), FuelType
(DIESEL/PETROL/LNG/JET_FUEL/HEAVY_FUEL_OIL/ELECTRIC/UNKNOWN), DistanceSource
(DOCUMENT/COMPUTED), ConfidenceLevel (HIGH/MEDIUM/LOW), ShipmentStatus (UPLOADED/
NEEDS_REVIEW/REVIEWED/CALCULATED/FAILED). BigDecimal for weight_tonnes, distance_km,
total_emissions_kgco2e, offset_tonnes. @ManyToOne to Vendor and User.
Create ShipmentRepository (paged filter by status and vendorId) and
ShipmentDocumentRepository (findByShipmentId). Also create
modules/shipment/exception/ShipmentException and register it in
GlobalExceptionHandler as 400. No services/controllers/DTOs yet. Boot; verify tables.
```

**Expected Output:** Created: 2 entities, 5 enums, 2 repositories, `ShipmentException` (+ handler mapping). Tables: `shipments`, `shipment_documents`.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A015`, `{STEP_SCOPE}=shipment entities`, extra:
```
13. \d shipments matches Section 7 column-for-column (NUMERIC precisions, indexes, default
    status UPLOADED, offset_tonnes default 0).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A015`, `{NEGATIVE_CASES}=duplicate s3_key insert fails (SQL)`, `{MOCKING_STRATEGY}=psql verification only`.

**Manual Testing:** Boot; `\d shipments`, `\d shipment_documents` in psql.

**Success Criteria:** [ ] Tables exact; enums STRING; BigDecimal money/tonnes.

**Next Step:** MANUAL SETUP C, then STEP A016.

---

### 🔧 MANUAL SETUP C — Amazon S3 Bucket + CORS + IAM User (do this YOURSELF, ~15 min)

Required before STEP A016. From COMMANDO.md Section 17.

**Sub-steps:**
1. AWS Console → S3 → **Create bucket**: name `carbontrace-docs-<yourname>` (globally unique), region = same as RDS (e.g. `ap-south-1`). Keep **Block ALL public access = ON** (defaults). Create.
2. Bucket → Permissions → **CORS** → paste exactly the JSON from COMMANDO.md Section 17 (AllowedOrigins `http://localhost:5173`, methods PUT/GET, ExposeHeaders ETag). Save.
3. IAM → Users → **Create user** `carbontrace-dev` (no console access).
4. Permissions → **Attach policies directly** → Create inline policy → JSON → paste the least-privilege policy from Section 17 with your bucket name (`s3:PutObject`, `s3:GetObject` on `arn:aws:s3:::carbontrace-docs-<yourname>/*` only). Name it `carbontrace-s3-rw`.
5. User → Security credentials → **Create access key** (use case: local code). Copy Access key ID + Secret.
6. Set env vars for the backend process: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION=<region>`, `S3_BUCKET=carbontrace-docs-<yourname>`.

**Verification of manual work:**
- [ ] `aws s3api put-object --bucket carbontrace-docs-<yourname> --key test.txt --body <(echo hi)` succeeds with these credentials (or verify via STEP A016's presigned PUT).
- [ ] Opening `https://<bucket>.s3.<region>.amazonaws.com/test.txt` in a browser → **Access Denied** (bucket is private — this MUST fail).
- [ ] `aws s3 ls` with this key fails (no ListBucket permission — least privilege confirmed).
- [ ] Keys exist only in env/.env (gitignored).

---

### STEP A016 — S3Service + Upload-URL and Document-URL Endpoints

**Objective:** Presigned PUT (upload-url) and presigned GET (document-url) with key convention and validation; prove a real PUT lands in the bucket.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A016 ONLY.
1. modules/shipment: create S3Service interface + S3ServiceImpl using the S3Presigner
   bean: generatePresignedPut(fileName, contentType, declaredSize) and
   generatePresignedGet(s3Key). Rules (Sections 8.4, 9, 17):
   - contentType must be application/pdf; declaredSize <= AppConstants.MAX_UPLOAD_BYTES;
     otherwise BadRequestException.
   - Key convention invoices/{yyyy}/{MM}/{uuid}-{sanitizedFileName}; sanitize to
     [a-zA-Z0-9._-].
   - PUT expiry 10 min, GET expiry 15 min (AppConstants). Sign PUT with the
     Content-Type condition. Never log full presigned URLs (truncate query string).
2. DTOs UploadUrlRequest (fileName, contentType) and UploadUrlResponse (uploadUrl,
   s3Key, expiresInSeconds).
3. ShipmentController (create it now, minimal) with ONLY:
   - POST /api/shipments/upload-url (AUDITOR+ADMIN) per Section 8.4.
   - GET /api/shipments/{id}/document-url (any role) — for now return 404 for unknown
     shipment via ShipmentDocumentRepository lookup; full shipment creation comes in
     STEP A017.
No shipment-creation endpoint yet. mvn compile + boot.
```

**Expected Output:** Created: `S3Service`, `S3ServiceImpl`, 2 DTOs, `ShipmentController` (2 endpoints), `ShipmentService` interface stub is NOT allowed — controller may call S3Service directly for these two routes or a minimal ShipmentService limited to these operations.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A016`, `{STEP_SCOPE}=presigned URL endpoints`, extra:
```
13. Only application/pdf accepted; 10 MB limit enforced at upload-url time.
14. Key convention + sanitization correct; expiries from AppConstants.
15. Presigned URLs never logged in full.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A016`, `{NEGATIVE_CASES}=contentType image/png → 400; size 11MB → 400; no token → 401`, `{MOCKING_STRATEGY}=real S3 with MANUAL SETUP C credentials`.

**Manual Testing:**
- `curl -X POST .../api/shipments/upload-url -H "Authorization: Bearer $T" -d '{"fileName":"test bill.pdf","contentType":"application/pdf"}'` → 200 with uploadUrl + s3Key like `invoices/2026/07/<uuid>-test_bill.pdf` (sanitized).
- `curl -X PUT "<uploadUrl>" -H 'Content-Type: application/pdf' --data-binary @sample.pdf` → 200; object visible in the S3 console.
- Expired-URL check: wait >10 min, retry PUT → 403 from S3.

**Success Criteria:** [ ] Real PDF lands in the private bucket via presigned PUT; validations enforced.

**Next Step:** STEP A017.

---

### STEP A017 — Shipment Create / List / Get (WITHOUT extraction)

**Objective:** Create shipments against uploaded S3 objects; extraction is stubbed per spec (status NEEDS_REVIEW, fields null) until A-SWAP-1.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A017 ONLY.
Extend modules/shipment with ShipmentCreateRequest (vendorId, s3Key, fileName,
fileSizeBytes), ShipmentResponseDto (every shipment field incl. per-field extraction
values and extractionConfidence), ShipmentMapper, ShipmentService/Impl methods and
controller routes per Section 8.4:
- POST /api/shipments (AUDITOR+ADMIN, 201): validate vendor exists AND is active
  (Section 9: no shipments for inactive vendors); create Shipment + ShipmentDocument
  (content_type application/pdf). Per the extraction-stub design (COMMANDO.md
  Section 20 reference note for this step): do NOT call FastAPI — set status
  NEEDS_REVIEW with all extracted fields null (extraction wired in A-SWAP-1).
- GET /api/shipments (any role): filters status, vendorId; PagedResponse.
- GET /api/shipments/{id} (any role).
- Complete GET /api/shipments/{id}/document-url to return a fresh presigned GET for the
  shipment's stored s3Key.
mvn compile + boot.
```

**Expected Output:** Created: 2 DTOs, `ShipmentMapper`, `ShipmentService`/`Impl`. Modified: `ShipmentController` (+3 routes). Rows insertable into both shipment tables.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A017`, `{STEP_SCOPE}=shipment CRUD (extraction stubbed)`, extra:
```
13. Inactive-vendor creation rejected 400; unknown vendor 404.
14. Status is NEEDS_REVIEW with null fields (stub by design until A-SWAP-1), NOT UPLOADED.
15. No FastAPI-calling extraction code exists in backend/ yet (it arrives at A-SWAP-1).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A017`, `{NEGATIVE_CASES}=inactive vendor, unknown vendorId, duplicate s3Key (409)`, `{MOCKING_STRATEGY}=real S3 object from STEP A016 reused`.

**Manual Testing:** Full flow: upload-url → PUT to S3 → POST /api/shipments → 201 with nulls + NEEDS_REVIEW → GET list/detail → document-url returns a working presigned GET (open in browser, PDF downloads).

**Success Criteria:** [ ] Upload→create→retrieve→document-url chain works end to end.

**Next Step:** STEP A018.

---

### STEP A018 — Shipment Review Endpoint + Status Transitions

**Objective:** Auditor review saves corrected fields and enforces the status machine.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A018 ONLY.
Add ShipmentReviewRequest (every field from the Section 8.4 review payload, correct
types, lat/lng range validation, weightTonnes positive, enums validated) and
PUT /api/shipments/{id}/review (AUDITOR+ADMIN):
- Allowed ONLY when status is NEEDS_REVIEW, REVIEWED, or FAILED; reject CALCULATED with
  ShipmentException 400 ("already calculated").
- distanceKm null means "compute later" (distance_source resolved at calculation).
- Persist all fields; set status REVIEWED; clear failure_reason.
mvn compile + boot.
```

**Expected Output:** Created: `ShipmentReviewRequest`. Modified: service/controller. Endpoint: review.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A018`, `{STEP_SCOPE}=review + status machine`, extra:
```
13. Status machine matches Section 9 exactly; CALCULATED is terminal for review.
14. Validation: lat [-90,90], lng [-180,180], weight > 0, enums constrained.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A018`, `{NEGATIVE_CASES}=review a CALCULATED shipment (prep via SQL UPDATE) → 400; lat 999 → 400; bad transportMode → 400`, `{MOCKING_STRATEGY}=SQL to force statuses`.

**Manual Testing:** Review the STEP A017 shipment with the Section 8.4 sample body → 200, status REVIEWED in psql; re-review allowed; force status CALCULATED via SQL → review → 400.

**Success Criteria:** [ ] Transitions enforced; fields persist; validation solid.

**Next Step:** STEP A019.

---

### STEP A019 — Emission Factor Module + Fallback Lookup + Seed

**Objective:** Factor reference CRUD (admin), the 4-level fallback query, and the GLOBAL/ANY seed rows.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A019 ONLY.
Create modules/emission per the pattern: EmissionFactor entity (Section 7 DDL, unique
(region, transport_mode, fuel_type), circuity default 1.20), EmissionFactorRepository
including a fallback lookup that returns the FIRST active match in this order
(Section 9): (originCountry,mode,fuel) → (originCountry,mode,'ANY') →
('GLOBAL',mode,fuel) → ('GLOBAL',mode,'ANY'). DTOs, mapper, EmissionFactorService/Impl,
EmissionFactorController:
- GET /api/emission-factors (any role; region, transportMode, page, size)
- POST /api/emission-factors (ADMIN)
- PUT /api/emission-factors/{id} (ADMIN)
- PUT /api/emission-factors/{id}/toggle-active (ADMIN)
Also create a TEMPORARY src/main/resources/data.sql seeding exactly the four GLOBAL/ANY
rows from Section 15 (idempotent inserts: ON CONFLICT DO NOTHING). Do NOT create
CalculationService (STEP A021). mvn compile + boot; verify the 4 seed rows.
```

**Expected Output:** Created: `modules/emission/**` (minus CalculationService), `data.sql`. Table: `emission_factors` + 4 rows. Endpoints: 4.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A019`, `{STEP_SCOPE}=emission factors`, extra:
```
13. Fallback order proven: with a (India,ROAD,DIESEL) row present, a lookup for
    (India,ROAD,DIESEL) hits it; for (India,ROAD,LNG) falls to (GLOBAL,ROAD,ANY) if no
    India/ANY row; inactive rows skipped.
14. Seed values match Section 15 exactly (factors + circuity per mode).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A019`, `{NEGATIVE_CASES}=duplicate (region,mode,fuel) POST → 409; auditor POST → 403`, `{MOCKING_STRATEGY}=SQL asserts for fallback via repository-backed endpoint calls in STEP A021's calculation`.

**Manual Testing:** psql `SELECT * FROM emission_factors;` → 4 rows; admin creates `India/ROAD/DIESEL`; auditor create → 403; Phase A3 checklist review.

**Success Criteria:** [ ] CRUD + fallback + seed done; Phase A3 checklist green.

**Next Step:** PHASE A4, STEP A020.

---

# PHASE A4 — CALCULATION & ANALYTICS, BACKEND SIDE (STEPS A020–A024)

**Purpose:** Build `mocks/mock-ai/`, then wire the calculation orchestration (FastAPI client side) against it, plus the map data endpoint, goals, and dashboard aggregates. The real FastAPI service is Track C's work: every AI call in this phase hits `mocks/mock-ai` (STEP A020); real-service verification happens at the A-SWAP steps.

**Prerequisites:** Phase A3 complete (shipments reviewable, factors seeded). Nothing from Track B or Track C.

**Deliverables:** `mocks/mock-ai/`; `reduction_goals` table; `/api/shipments/{id}/calculate`, `/api/shipments/map`, goal CRUD, `/api/analytics/dashboard`.

**Dependencies:** Shipment status machine (STEP A018), factor fallback (STEP A019), RestTemplate + AiConfig (STEP A002).

**Phase completion checklist:**
- [ ] Calculate endpoint validates prerequisites, persists mock-ai's canned result on the happy path, and returns 502 "AI service unavailable — please try again" when mock-ai is stopped
- [ ] Map endpoint returns only CALCULATED shipments with full coordinates
- [ ] Dashboard aggregates compute from live data (verified with SQL-crafted rows)
- [ ] Goal rules enforced (target < baseline, year >= current)
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### STEP A020 — Build mocks/mock-ai (Template 0.7)

**Objective:** A stand-in for the FastAPI service (POST /extract, POST /calculate, POST /agent/purchase, GET /health) on port 8000, per COMMANDO.md Section 25.3, so STEP A021 (CalculationService) and STEP A026 (purchase commit path) exercise the production HTTP path against real network responses.

**Prompt to Claude Code / Antigravity:** Use template 0.7 with `{MOCK_NAME}=mock-ai`, `{STANDS_IN_FOR}=the FastAPI AI service (COMMANDO.md Section 8.11)`, `{ENDPOINTS}=POST /extract, POST /calculate, POST /agent/purchase, GET /health`, `{PORT}=8000`, `{TOOLING}=WireMock standalone or a ~50-line Python script — it must NOT require the AI track's codebase (Section 25.3)`, `{SWAP_STEP}=A-SWAP-1 (/extract), A-SWAP-2 (/calculate), A-SWAP-3 (/agent/purchase) — one route deleted per swap step`, appending:
```
Canned behavior (verbatim Section 8.11 examples, snake_case):
- POST /extract → the Section 8.11 /extract example response (Shenzhen→Rotterdam, SEA,
  weight_tonnes 18.5, the example field_confidence map, overall_confidence MEDIUM).
- POST /calculate → the Section 8.11 /calculate example response (distance_km 20430.00,
  distance_source "COMPUTED", total_emissions_kgco2e 4157.51, the formula string).
- POST /agent/purchase → the Section 8.11 PURCHASE example (listing_id 3, tonnes 4.219,
  price_per_tonne_usd 14.50, total_cost_usd 61.18, transaction_reference SIM-9F3A2C71)
  — EXCEPT when max_budget_usd is present and <= 20, then the Section 8.11 NO_PURCHASE
  example. This single budget rule exists only so Spring Boot's NO_PURCHASE handling is
  testable; invent nothing else.
- GET /health → {"status": "ok"}.
```

**Expected Output:** `mocks/mock-ai/` (server + README stating run command, port 8000, retiring swap steps, "MOCK — replaced in A-SWAP-1/2/3" label). No backend code touched.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A020`, `{STEP_SCOPE}=mock-ai stand-in`, extra:
```
13. Every canned body diffs clean against its Section 8.11 example; snake_case keys; no
    invented fields; the budget<=20 rule is the ONLY behavior beyond the examples.
14. The mock has no dependency on ai-service/ code and is never imported by backend code.
15. README + "MOCK — replaced in ..." labels present per Section 25.4.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A020`, `{NEGATIVE_CASES}=malformed JSON body → 4xx; /agent/purchase with max_budget_usd 20 → the NO_PURCHASE example`, `{MOCKING_STRATEGY}=none — this step IS the mock; curl it directly`.

**Manual Testing:** Start it on :8000; curl all four routes; diff each response against its COMMANDO.md Section 8.11 example (they must be identical).

**Success Criteria:** [ ] Four routes answer with verbatim Section 8.11 shapes; README + labels in place.

**Next Step:** STEP A021.

---

### STEP A021 — CalculationService (FastAPI /calculate Client)

**Objective:** Orchestrate factor lookup → request assembly → FastAPI call → persistence; happy path against mocks/mock-ai (STEP A020), 502 error path with the mock stopped.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A021 ONLY.
In modules/emission create CalculationService + CalculationServiceImpl and add
POST /api/shipments/{id}/calculate (AUDITOR+ADMIN) to ShipmentController, behavior
exactly per Section 8.4:
1. Require status REVIEWED; require transportMode, weightTonnes > 0, and either
   distanceKm > 0 or all four coordinates — else 400 with a precise message.
2. Factor lookup via the STEP A019 fallback chain using originCountry; 400 if none.
3. Build the snake_case JSON payload from Section 8.11 /calculate (all data in the
   payload — shipment fields + factor + circuity) and POST via RestTemplate to
   {AiConfig.fastapiBaseUrl}/calculate. Timeouts: connect 5s, read 15s for this call.
4. On success: persist distance_km, distance_source, total_emissions_kgco2e; status
   CALCULATED; return the Section 8.4 response.
5. On RestTemplate connection failure: the GlobalExceptionHandler 502 path applies
   ("AI service unavailable — please try again"); shipment stays REVIEWED (calculation
   failures do NOT set FAILED — that is extraction-only per Section 23).
Field-name mapping snake_case↔camelCase must be explicit (@JsonProperty on the internal
request/response DTOs). The real FastAPI is Track C's work and is NOT required here:
mocks/mock-ai (STEP A020) answers on :8000 with the canned Section 8.11 /calculate
example — verify the happy path against it AND the 502 path with the mock stopped.
mvn compile + boot.
```

**Expected Output:** Created: `CalculationService`, `CalculationServiceImpl`, internal FastAPI request/response DTOs. Modified: `ShipmentController`. Endpoint: calculate.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A021`, `{STEP_SCOPE}=calculation orchestration`, extra:
```
13. Payload keys are snake_case and match Section 8.11 /calculate exactly.
14. Precondition validation matches Section 9 calculation requirements.
15. Failure leaves the shipment REVIEWED and returns 502 with the exact message.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A021`, `{NEGATIVE_CASES}=status NEEDS_REVIEW → 400; missing coords AND distance → 400; no matching factor (SQL-deactivate GLOBAL rows temporarily) → 400; mock-ai stopped → 502`, `{MOCKING_STRATEGY}=mocks/mock-ai (STEP A020) running on :8000 for the happy path; stop it for the 502 path`.

**Manual Testing:** With mocks/mock-ai running: review a shipment → calculate → 200 with the mock's canned Section 8.11 values persisted (distance_km 20430.00, distance_source COMPUTED, total_emissions_kgco2e 4157.51), status CALCULATED in psql. Stop mock-ai → calculate another reviewed shipment → 502 exact message; psql: status still REVIEWED. Deactivate seed rows → 400 factor message; reactivate.

**Success Criteria:** [ ] Preconditions, factor chain, payload shape, mock-ai happy path, and 502 path all correct.

**Next Step:** STEP A022.

---

### STEP A022 — Shipments Map Endpoint

**Objective:** `/api/shipments/map` returning `ShipmentMapPointDto` for CALCULATED shipments with full coordinates.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A022 ONLY.
Add ShipmentMapPointDto (id, originLat/Lng, destinationLat/Lng, originCity,
destinationCity, transportMode, totalEmissionsKgco2e, status) and
GET /api/shipments/map (any role) returning ONLY shipments with status CALCULATED and
all four coordinates non-null, per Section 8.4. Repository-level filtering, not
in-memory. mvn compile + boot.
```

**Expected Output:** Created: DTO + repository query. Endpoint: map.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A022`, `{STEP_SCOPE}=map endpoint`, extra:
```
13. Filtering happens in the query; non-CALCULATED and partial-coordinate rows excluded.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A022`, `{NEGATIVE_CASES}=REVIEWED shipment with coords excluded; CALCULATED without coords excluded`, `{MOCKING_STRATEGY}=SQL: hand-set one shipment to CALCULATED with coords + emissions`.

**Manual Testing:** SQL-craft one CALCULATED shipment → GET /api/shipments/map → exactly that row.

**Success Criteria:** [ ] Correct filtering + DTO shape.

**Next Step:** STEP A023.

---

### STEP A023 — Goal Module

**Objective:** Reduction goal CRUD with Section 9 rules.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A023 ONLY.
Create modules/goal per the pattern: ReductionGoal entity (Section 7 DDL),
GoalRequestDto (validation: targetEmissions < baselineEmissions enforced in service;
targetYear >= current year), GoalResponseDto, GoalMapper, GoalService/Impl,
GoalController with POST/GET/GET{id}/PUT{id}/DELETE{id} /api/goals (AUDITOR+ADMIN)
per Section 8.8. progressPercent is NOT computed here (analytics, STEP A024).
mvn compile + boot; verify reduction_goals table.
```

**Expected Output:** Created: `modules/goal/**`. Table: `reduction_goals`. Endpoints: 5.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A023`, `{STEP_SCOPE}=goal module`, extra:
```
13. target < baseline and targetYear >= current year rejected with 400 messages.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A023`, `{NEGATIVE_CASES}=target >= baseline → 400; targetYear 2020 → 400; delete unknown id → 404`, `{MOCKING_STRATEGY}=none`.

**Manual Testing:** Create the Section 8.8 sample goal → CRUD round trip; psql check.

**Success Criteria:** [ ] CRUD + both business rules.

**Next Step:** STEP A024.

---

### STEP A024 — Analytics Module (Dashboard Aggregates)

**Objective:** `/api/analytics/dashboard` computing all aggregates live per Section 8.9.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A024 ONLY.
Create modules/analytics: DashboardDto matching Section 8.9 exactly (totalShipments,
calculatedShipments, totalEmissionsKgco2e, totalOffsetTonnes, netEmissionsKgco2e,
totalOffsetSpendUsd, emissionsByMode map, emissionsByVendor list, monthlyEmissions —
last 6 months oldest-first with yyyy-MM keys — and goals with computed progressPercent),
AnalyticsService/Impl using aggregate repository queries (nothing stored separately),
AnalyticsController GET /api/analytics/dashboard (any role).
progressPercent = clamp((baseline − currentNetAnnualEmissions)/(baseline − target)×100,
0, 100) per Section 9, where currentNetAnnualEmissions = current-year calculated
emissions minus current-year offset tonnes × 1000.
totalOffsetSpendUsd sums offset_purchases.total_cost_usd — the purchases table does not
exist yet (STEP A026), so implement this aggregate to return 0 via a guarded query
against shipment.offset_tonnes-only data and add offset-spend wiring as an explicit
follow-up in STEP A026's scope note — do NOT create the purchases entity now. Simplest
compliant approach: compute totalOffsetTonnes from shipments.offset_tonnes and
totalOffsetSpendUsd as BigDecimal.ZERO with a clear comment referencing STEP A026.
mvn compile + boot.
```

**Expected Output:** Created: `modules/analytics/**`. Endpoint: dashboard.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A024`, `{STEP_SCOPE}=analytics`, extra:
```
13. monthlyEmissions covers exactly the last 6 months, oldest first, yyyy-MM keys.
14. All aggregates are query-computed; no new tables; purchases entity NOT created.
15. progressPercent clamped to [0,100].
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A024`, `{NEGATIVE_CASES}=empty DB returns zeros/empty maps not errors`, `{MOCKING_STRATEGY}=SQL-craft shipments across months/modes/vendors then assert JSON`.

**Manual Testing:** With SQL-crafted data across 3 months and 2 modes: dashboard JSON matches manual sums; empty DB → zeros.

**Success Criteria:** [ ] All fields present + numerically correct; Phase A4 checklist green (real-service calculate verification pending A-SWAP-2 by design).

**Next Step:** PHASE A5, STEP A025.

---

# PHASE A5 — MARKETPLACE & PURCHASES, BACKEND SIDE (STEPS A025–A027)

**Purpose:** The simulated marketplace (public reads + admin writes), the transactional purchase commit path (tested against mocks/mock-ai's canned agent response), and admin user management.

**Prerequisites:** Phase A4 complete.

**Deliverables:** `sellers`, `carbon_credit_listings`, `offset_purchases` tables; Sections 8.6, 8.7, 8.10 endpoints; seed of 3 sellers + 8 listings; dashboard offset-spend completed.

**Dependencies:** Auth roles, shipments CALCULATED status, analytics (spend wiring).

**Phase completion checklist:**
- [ ] Public marketplace GETs work WITHOUT a token
- [ ] Purchase commit path: pessimistic lock, re-validation, decrement, snapshot, SIM reference — proven against mock-ai's canned agent response
- [ ] available_tonnes can never go negative; 409 on stale listing
- [ ] Admin can toggle users active/inactive
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### STEP A025 — Marketplace Module (Sellers + Listings + Public/Admin Endpoints + Seed)

**Objective:** Marketplace entities, public filtered reads, admin management, and demo seed data.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A025 ONLY.
Create modules/marketplace per the pattern: Seller and CarbonCreditListing entities
(Section 7 DDL; enums VerificationStandard VCS/GOLD_STANDARD/CDM and ProjectType
REFORESTATION/RENEWABLE_ENERGY/METHANE_CAPTURE/SOIL_CARBON as STRING; BigDecimal for
price/tonnes/rating), repositories with the filtered listing query (projectType,
maxPricePerTonne, minAvailableTonnes, only active listings of active sellers, paged),
DTOs, MarketplaceMapper, MarketplaceService/Impl, and TWO controllers:
- MarketplaceController (PUBLIC, matches SecurityConfig GET permit):
  GET /api/marketplace/credits (filters + paging per Section 8.6, response shape
  matching the example incl. seller fields), GET /api/marketplace/credits/{id},
  GET /api/marketplace/sellers, GET /api/marketplace/sellers/{id}.
- MarketplaceAdminController (ADMIN): POST/PUT /api/admin/marketplace/sellers[/{id}],
  POST/PUT /api/admin/marketplace/credits[/{id}] (price, tonnes restock, active flag).
Extend data.sql to seed 3 sellers + 8 listings (varied standards, prices $8–$25/t,
availability, vintages; include the Section 8.6 example listing). Idempotent inserts.
mvn compile + boot; confirm seeds.
```

**Expected Output:** Created: `modules/marketplace/**` (2 controllers). Tables: `sellers`, `carbon_credit_listings` + seed rows. Endpoints: 4 public + 4 admin.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A025`, `{STEP_SCOPE}=marketplace`, extra:
```
13. curl WITHOUT any token succeeds on all 4 public GETs; POST/PUT without admin → 401/403.
14. Listing response embeds sellerName/Country/Rating/verificationStandard per 8.6.
15. Filters combine correctly and exclude inactive rows.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A025`, `{NEGATIVE_CASES}=admin write as auditor → 403; unknown listing id → 404; filter maxPricePerTonne=10 excludes pricier rows`, `{MOCKING_STRATEGY}=none — seeded data`.

**Manual Testing:** `curl localhost:8080/api/marketplace/credits?maxPricePerTonne=15` (no auth) → filtered page; admin restock tonnes → reflected.

**Success Criteria:** [ ] Public reads tokenless; admin writes gated; 8 listings seeded (the variety covers the agent's decision cases: cheap/expensive, low/high availability, different verification standards).

**Next Step:** STEP A026. Also tick the "Real marketplace GETs" row in the Integration Readiness Checklist now — Track C's C-SWAP-1 consumes it.

---

### STEP A026 — OffsetPurchase Entity + Transactional Commit Path

**Objective:** The purchase transaction (lock → re-validate → decrement → snapshot) proven against mocks/mock-ai's canned agent responses; dashboard spend completed.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A026 ONLY.
1. modules/purchase: OffsetPurchase entity (Section 7 DDL: snapshots, unique
   transaction_reference, agent_reasoning TEXT NOT NULL, status COMPLETED/FAILED),
   OffsetPurchaseRepository, PurchaseRequestDto (shipmentId, optional maxBudgetUsd),
   PurchaseResponseDto (Section 8.7 shape), PurchaseMapper, PurchaseException
   (register 409 in GlobalExceptionHandler).
2. CarbonCreditListingRepository: add findByIdForUpdate with
   @Lock(LockModeType.PESSIMISTIC_WRITE).
3. PurchaseService/Impl implementing the Section 16 commit path EXACTLY:
   @Transactional purchase(shipmentId, maxBudgetUsd, userId):
   - shipment must be CALCULATED; requiredTonnes = ceil to 3 decimals of
     max(totalKg/1000 − offsetTonnes, 0); PurchaseException/400 if zero
     ("already fully offset").
   - Call the agent via an internal AgentClient component that POSTs Section 8.11's
     /agent/purchase payload (snake_case, marketplace_base_url from AiConfig) with
     RestTemplate (read timeout 120s). The real agent is Track C's work and is NOT
     required here: mocks/mock-ai (STEP A020) answers on :8000 with the canned
     Section 8.11 PURCHASE example targeting listing id 3 (the Section 8.6 example
     listing), or the canned NO_PURCHASE example when max_budget_usd <= 20. Use the
     production HTTP path as-is — NO dev-only flags, NO seams; A-SWAP-3 changes only
     which process listens on :8000.
   - On decision NO_PURCHASE: return the agent's reasoning with NO row written
     (ApiResponse.error semantics per Sections 8.7/16).
   - On PURCHASE: load listing with the pessimistic lock; validate active,
     availableTonnes >= tonnes, |listingPrice − agentPrice| <= 0.01 — else
     PurchaseException 409 (no row); decrement availableTonnes; increment
     shipment.offsetTonnes; save OffsetPurchase with snapshots, reasoning, SIM
     reference; purchases are immutable (no update/delete endpoints ever).
4. PurchaseController: POST /api/purchases (201), GET /api/purchases (filters
   shipmentId, paging), GET /api/purchases/{id} — per Section 8.7.
5. Complete AnalyticsServiceImpl: totalOffsetSpendUsd now sums
   offset_purchases.total_cost_usd (replace the STEP A024 ZERO with the real query).
mvn compile + boot; verify offset_purchases table.
```

**Expected Output:** Created: `modules/purchase/**`, lock query. Modified: `GlobalExceptionHandler`, `AnalyticsServiceImpl`. Table: `offset_purchases`. Endpoints: 3.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A026`, `{STEP_SCOPE}=purchase commit path`, extra:
```
13. Pessimistic lock present; all three re-validations; no row on 409/NO_PURCHASE.
14. requiredTonnes rounds UP to 3 decimals; zero → 400 "already fully offset".
15. available_tonnes cannot go negative under any input; snapshots + verbatim reasoning
    stored; SIM- reference unique.
16. The agent call uses the production RestTemplate HTTP path — no dev-only flags or
    seams; mock vs real is decided ONLY by which process listens at
    app.fastapi.base-url.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A026`, `{NEGATIVE_CASES}=shipment not CALCULATED → 400; fully offset → 400; listing tonnes too low (SQL shrink) → 409; price drift > $0.01 (SQL) → 409; concurrent double-purchase attempt documented`, `{MOCKING_STRATEGY}=mocks/mock-ai running on :8000 (canned agent responses per STEP A020)`.

**Manual Testing:**
- SQL: set a shipment CALCULATED with total 4218.75 kg. mock-ai running → POST /api/purchases (budget 200) → mock returns the canned PURCHASE for listing 3 → 201, tonnes 4.219, listing 3 decremented, shipment.offset_tonnes=4.219, dashboard spend > 0.
- POST /api/purchases with maxBudgetUsd 20 on another CALCULATED shipment → mock returns the canned NO_PURCHASE → error response carrying the agent's explanation, NO row written.
- Repeat purchase on the offset shipment → 400 fully offset. SQL price change → 409, row count unchanged. Stop mock-ai → 502 "AI service unavailable" path.

**Success Criteria:** [ ] Whole transactional path (incl. NO_PURCHASE) proven against mock-ai — no real agent needed.

**Next Step:** STEP A027.

---

### STEP A027 — Admin Module (User Management)

**Objective:** Admin user listing + activate/deactivate.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP A027 ONLY.
Create modules/admin: AdminService/Impl and AdminController with
GET /api/admin/users (role filter, paging; reuses UserResponseDto) and
PUT /api/admin/users/{id}/toggle-active per Section 8.10, both ROLE_ADMIN.
Deactivated users must fail login (via STEP A009 semantics). mvn compile + boot.
```

**Expected Output:** Created: `modules/admin/**`. Endpoints: 2.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP A027`, `{STEP_SCOPE}=admin users`, extra:
```
13. Deactivated user's login attempt is rejected; auditor access → 403.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A027`, `{NEGATIVE_CASES}=auditor calls → 403; toggle unknown id → 404; deactivated user login → 401`, `{MOCKING_STRATEGY}=none`.

**Manual Testing:** Admin lists users, deactivates the auditor → auditor login fails → reactivate. Phase A5 checklist review.

**Success Criteria:** [ ] Backend feature-complete except real-FastAPI wiring; Phases A1–A5 checklists green.

**Next Step:** Track A's build phases are done. Run template 0.9 for Phase A5, then tick Track A's provider rows in the Integration Readiness Checklist (real backend auth+CRUD and real S3 presigned flow → consumed by B-SWAP-1; real marketplace → consumed by C-SWAP-1). Proceed to A-SWAP-1 as soon as Track C ticks the "Real /extract" row; the swap steps below are the only remaining Track A work.

---

# TRACK A — INTEGRATION SWAP STEPS (A-SWAP-1, A-SWAP-2, A-SWAP-3)

These are Track A's only steps with cross-track preconditions. Each one follows template 0.8, swaps ONE mock-ai route for the real FastAPI service, re-runs the original step's tests against the real dependency, and DELETES that mock route (COMMANDO.md Section 25.4). Run template 0.9 before each swap. Execute them in order as Track C ticks the corresponding rows in the Integration Readiness Checklist (Shared Integration phase); until a row is ticked, nothing in Track A is blocked — the swap steps are simply the only remaining Track A work.

---

### A-SWAP-1 — Swap /extract: Wire ExtractionService to the Real FastAPI (retires mock-ai /extract)

**Objective:** Replace the STEP A017 stub: shipment creation now synchronously calls the REAL FastAPI /extract; FAILED path handled; the mock-ai /extract route is deleted.

**Precondition:** Track C has ticked "Real /extract" in the Integration Readiness Checklist (STEP C004 complete); the real `ai-service` can run locally on :8000. The PDF fixtures in `mocks/sample-pdfs/` remain available for test uploads.

**Prompt to Claude Code / Antigravity:** Use template 0.8 with `{SWAP_ID}=A-SWAP-1`, `{MOCK}=mocks/mock-ai's /extract route`, `{REAL_DEPENDENCY}=the real FastAPI POST /extract (Track C, STEP C004)`, `{CONFIG_CHANGE}=none — app.fastapi.base-url already points at http://localhost:8000; stop mock-ai and run the real ai-service there`, `{ORIGINAL_TESTS}=testing/track-a/step-A017-tests.sh plus the new extraction cases below`, preceded by this one-time wiring work (the backend half of the extraction integration, which by design could not exist before the swap):
```
Read COMMANDO.md and implement the A-SWAP-1 wiring ONLY.
In backend modules/extraction create ExtractionService + ExtractionServiceImpl and
ExtractionResultDto (mirrors the FastAPI ExtractResponse; explicit @JsonProperty for
snake_case mapping). Modify ShipmentServiceImpl's create flow to match Section 8.4:
1. Create shipment (status UPLOADED) + document row.
2. Generate a presigned GET (15 min) for the s3Key.
3. SYNCHRONOUSLY POST {fastapiBaseUrl}/extract with shipment_id, document_url,
   file_name (RestTemplate; connect 5s / read 90s for THIS call per Section 18).
4. Success: persist all extracted fields + per-field/overall confidence → NEEDS_REVIEW.
5. Any failure (422/502/connection): status FAILED with failure_reason set from the
   error; shipment creation still returns 201 so the auditor proceeds manually
   (Section 23). Store per-field confidence in a way the ShipmentResponseDto already
   exposes (add a fieldConfidence map to the DTO if STEP A017 did not).
Remove the STEP A017 "always NEEDS_REVIEW with nulls" stub.
First smoke-test the request/response mapping against mock-ai's /extract (its canned
Section 8.11 example), then swap to the real ai-service, re-run, and diff behavior.
Then delete the mock-ai /extract route and its README entry. Any contract mismatch is
a Section 8 discrepancy (template 0.10) — never adapt silently. mvn compile + boot.
```

**Expected Output:** Created: `modules/extraction/**`. Modified: `ShipmentServiceImpl`, possibly `ShipmentResponseDto`/mapper. Deleted: mock-ai /extract route + its README entry.

**Verification Prompt:** Template 0.3, `{STEP_ID}=A-SWAP-1`, `{STEP_SCOPE}=extraction wiring + mock route retirement`, extra:
```
13. Call is strictly synchronous; no async/queues; timeouts per Section 18 Flow 4.
14. FAILED shipments carry failure_reason and remain reviewable (STEP A018 machine).
15. STEP A017 stub fully removed; UPLOADED→NEEDS_REVIEW/FAILED transition correct.
16. mock-ai's /extract route is gone; its /calculate and /agent/purchase routes remain
    (their swaps come later); no business logic changed beyond the listed wiring.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A-SWAP-1`, `{NEGATIVE_CASES}=ai-service stopped → shipment FAILED with 502-derived reason (creation still 201); blank/image-only PDF → FAILED with the OCR message`, `{MOCKING_STRATEGY}=real FastAPI + real S3; PDF fixtures from mocks/sample-pdfs`.

**Manual Testing:** Backend + real ai-service running: upload the clean sample PDF (via curl/Postman: upload-url → PUT → POST /api/shipments; or via the UI if Track B has already completed B-SWAP-1) → shipment NEEDS_REVIEW with real extracted fields and confidence map; stop the ai-service → upload → FAILED with failure_reason, manual review still works; diff the real /extract response against the retired mock's canned example — identical shape, real values.

**Success Criteria:** [ ] Real extraction populates shipments; FAILED path graceful; mock /extract route deleted.

**Next Step:** A-SWAP-2 (once Track C ticks "Real /calculate" — usually already ticked, since STEP C003 precedes C004).

---

### A-SWAP-2 — Swap /calculate: End-to-End Calculation Against the Real Service (retires mock-ai /calculate)

**Objective:** Prove upload → extract → review → calculate against the real factor table and the REAL FastAPI /calculate (replacing mock-ai's canned response); fix integration defects only; delete the mock-ai /calculate route.

**Precondition:** Track C has ticked "Real /calculate" (STEP C003 complete); A-SWAP-1 done (the chain includes real extraction).

**Prompt to Claude Code / Antigravity:** Use template 0.8 with `{SWAP_ID}=A-SWAP-2`, `{MOCK}=mocks/mock-ai's /calculate route`, `{REAL_DEPENDENCY}=the real FastAPI POST /calculate (Track C, STEP C003)`, `{CONFIG_CHANGE}=none — the real ai-service already listens on :8000 since A-SWAP-1`, `{ORIGINAL_TESTS}=testing/track-a/step-A021-tests.sh`, plus:
```
Read COMMANDO.md and execute A-SWAP-2 ONLY. This is an integration-verification step:
implement NO new features. With RDS, Spring Boot, and the real ai-service running:
1. Drive the full chain with the clean sample PDF (Shenzhen→Rotterdam SEA 18.5t):
   upload → extract → review (confirm/adjust fields, leave distanceKm null) → calculate.
2. Assert: factor chain resolves (China/SEA/HEAVY_FUEL_OIL if seeded, else GLOBAL/SEA/
   ANY), distance_source COMPUTED, total = weight × distance × factor to 3dp, status
   CALCULATED, map endpoint now includes the shipment, dashboard totals update.
   Note the change from the mock: mock-ai always returned the canned 20430.00 /
   4157.51 example; the real service computes from YOUR payload. Diff shape (must be
   identical) and values (now genuinely computed).
3. Re-run with distanceKm provided in review → distance_source DOCUMENT, circuity NOT
   applied.
4. Fix ONLY genuine integration bugs found (field-name mismatches, rounding, mapping),
   each with a minimal diff, and list every fix made. A contract disagreement is a
   Section 8 discrepancy (template 0.10) — never adapt silently.
5. Delete the mock-ai /calculate route and its README entry.
Produce testing/track-a/step-A-SWAP-2-e2e-report.md documenting inputs, expected vs
actual, fixes.
```

**Expected Output:** No new features. Possible small fixes. Created: e2e report. Deleted: mock-ai /calculate route.

**Verification Prompt:** Template 0.3, `{STEP_ID}=A-SWAP-2`, `{STEP_SCOPE}=integration fixes only`, extra:
```
13. Every change is a bug fix traceable to the report; no scope creep; no business
    logic changed by the swap itself.
14. mock-ai now serves ONLY /agent/purchase and /health.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A-SWAP-2`, `{NEGATIVE_CASES}=calculate before review → 400; recalculation attempt on CALCULATED via review → 400`, `{MOCKING_STRATEGY}=fully real backend + ai-service; PDF fixtures from mocks/sample-pdfs`.

**Manual Testing:** As scripted above; cross-check total emissions with a calculator; verify psql values match the API response.

**Success Criteria:** [ ] Both distance modes correct; map + dashboard reflect the shipment; mock /calculate route deleted.

**Next Step:** A-SWAP-3 (once Track C ticks "Real /agent/purchase" — that requires Track C's C-SWAP-1, which in turn consumes this track's A025 marketplace).

---

### A-SWAP-3 — Swap /agent/purchase: Real Agent Behind the Purchase Commit Path (retires mock-ai entirely)

**Objective:** The purchase commit path (STEP A026) now runs against the REAL FastAPI agent browsing the REAL marketplace; the last mock-ai route — and with it the whole `mocks/mock-ai/` directory — is deleted.

**Precondition:** Track C has ticked "Real /agent/purchase" (STEP C005 AND C-SWAP-1 complete — the agent must already browse the real Spring Boot marketplace).

**Prompt to Claude Code / Antigravity:** Use template 0.8 with `{SWAP_ID}=A-SWAP-3`, `{MOCK}=mocks/mock-ai (its last remaining route, /agent/purchase — so the entire directory goes)`, `{REAL_DEPENDENCY}=the real FastAPI POST /agent/purchase (Track C, STEP C005 + C-SWAP-1)`, `{CONFIG_CHANGE}=none — the real ai-service already listens on :8000`, `{ORIGINAL_TESTS}=testing/track-a/step-A026-tests.sh`, plus:
```
Read COMMANDO.md and execute A-SWAP-3 ONLY. Change NO business logic — the
PurchaseServiceImpl HTTP path built in STEP A026 is already the production path.
1. With backend + real ai-service running, re-run the STEP A026 cases:
   a. Purchase with budget 200 on a CALCULATED shipment → 201; the agent's REAL
      reasoning text stored verbatim (no longer the canned Section 8.11 sentence);
      SIM- reference format per Section 9; listing decremented; shipment.offset_tonnes
      incremented.
   b. maxBudgetUsd too low for any viable listing → NO_PURCHASE explanation surfaced,
      NO row written.
   c. Price drift > $0.01 (SQL) → 409, no row.
2. Diff each observed response against the retired mock's canned Section 8.11 examples:
   shapes identical, values now real. Any contract mismatch is a Section 8 discrepancy
   (template 0.10) — never adapt silently.
3. DELETE mocks/mock-ai entirely (directory + README). Confirm with git status and
   verify no reference to mock-ai remains anywhere in backend code, config, or scripts.
```

**Expected Output:** No code changes beyond possible Section 8-justified bug fixes. Deleted: `mocks/mock-ai/`.

**Verification Prompt:** Template 0.3, `{STEP_ID}=A-SWAP-3`, `{STEP_SCOPE}=agent swap + mock-ai retirement`, extra:
```
13. Zero references to mocks/mock-ai remain in code, config, scripts, or docs.
14. NO_PURCHASE and 409 leave the database bit-for-bit unchanged (row counts + tonnes).
15. No business logic changed by the swap.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=A-SWAP-3`, `{NEGATIVE_CASES}=fully-offset shipment re-purchase → 400; ai-service stopped → 502 "AI service unavailable — please try again"`, `{MOCKING_STRATEGY}=fully real backend + ai-service + Gemini`.

**Manual Testing:** Watch the ai-service logs during a purchase: only GET tool calls hit the marketplace; reasoning is coherent and mentions compared listings; `SELECT SUM(available_tonnes)` arithmetic checks out.

**Success Criteria:** [ ] Real agent purchases commit end to end; mocks/mock-ai deleted; Track A fully mock-free.

**Next Step:** Tick the three A-SWAP rows as done. When ALL five swap steps (A-SWAP-1..3, B-SWAP-1, C-SWAP-1) are complete, merge `track-a` to `main` and join the SHARED INTEGRATION PHASE at STEP I001.

---

# ═══ TRACK B — FRONTEND (Developer B starts here on day one) ═══

Track B owns `frontend/` and `testing/track-b/`. It builds `mocks/mock-backend/` (STEP B002) so every backend dependency — auth, CRUD, uploads, analytics — is served locally from day one. Work on branch `track-b`; verification prompts use template 0.3B. Nothing in Phase B1 depends on Track A or Track C until B-SWAP-1.

# PHASE B1 — FRONTEND (STEPS B001–B006 + B-SWAP-1)

**Purpose:** The complete React UI, built and manually tested end to end against `mocks/mock-backend` (port 8080), then swapped onto the real backend + real S3 in B-SWAP-1. Every Manual Testing block in this phase runs against the mock, with the expected mock responses spelled out.

**Prerequisites:** Node.js 22 LTS installed (`node -v` → v22.x). NOTHING from Track A or Track C — the mock backend stands in for every server dependency until B-SWAP-1.

**Deliverables:** All 14 pages, auth context with refresh interceptor, the full direct-to-S3 upload UX (against the mock's fake presigned endpoint until B-SWAP-1), map, dashboard, marketplace with "Simulated" banner, admin tabs; `mocks/mock-backend/`.

**Dependencies:** None outside this track — only the COMMANDO.md Section 8 contract, embodied by the mock.

**Phase completion checklist:**
- [ ] Register→OTP→login→dashboard in the browser (against mock-backend)
- [ ] PDF drag-drop runs the full presigned-PUT upload UX with progress and creates a shipment (mock)
- [ ] Review form highlights low-confidence fields (mock serves LOW/MEDIUM confidences)
- [ ] Map + charts render; admin tabs gated; simulated-marketplace banner persistent
- [ ] Every page has loading / error / empty states (mock error routes exercised)
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### STEP B001 — React Project Setup + Folder Skeleton

**Objective:** Vite scaffold, dependency set, design tokens, folder skeleton, env files — nothing that needs any server.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP B001 ONLY.
Create ./frontend with Vite + React 18 and ONLY these deps (Section 4): react-router-dom@6,
axios, react-hook-form, react-leaflet + leaflet, recharts. No Tailwind, no UI kits,
no Redux, no React Query.
- src/styles/global.css: CSS custom properties — sustainability palette (greens/earth
  tones), spacing, typography; clean corporate look.
- The folder skeleton exactly per COMMANDO.md Section 6 (api/, context/, components/
  {common,auth,vendor,shipment,emissions,marketplace,dashboard,admin}/, pages/,
  styles/) — folders only, no feature components yet.
- App.jsx + main.jsx rendering a minimal shell (app title + router provider with a
  single index route) — no dead links, no placeholder pages.
- frontend/.env with VITE_API_BASE_URL=http://localhost:8080/api and a
  frontend/.env.example. Create frontend/.gitignore covering .env — the repo-root
  .gitignore is Track A's (STEP A001) and only reaches this branch at the integration
  merge, so Track B must not depend on it. This value points at mocks/mock-backend
  (built next, STEP B002) and NEVER changes — at B-SWAP-1 only the process listening
  on :8080 changes.
npm run dev must start clean. Do not build auth plumbing or feature pages yet.
No placeholders, no TODOs.
```

**Expected Output:** Created: `frontend/**` scaffold (deps, global.css, folder skeleton, shell App/main, env files). No components, no API code.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=STEP B001`, `{STEP_SCOPE}=frontend scaffold`, extra:
```
13. Dependency list contains ONLY Section 4 frontend packages.
14. Folder tree matches Section 6; design tokens centralized in global.css.
15. .env gitignored; .env.example present.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B001`, `{NEGATIVE_CASES}=none (no endpoints yet — script documents npm run dev + npm run build exit codes)`, `{MOCKING_STRATEGY}=none required`.

**Manual Testing:** `npm run dev` → shell renders at :5173; `npm run build` succeeds; no console errors.

**Success Criteria:** [ ] Scaffold clean; only Section 4 deps; skeleton matches Section 6; builds.

**Next Step:** STEP B002.

---

### STEP B002 — Build mocks/mock-backend (Template 0.7)

**Objective:** A complete local stand-in for every Spring Boot endpoint the frontend consumes (COMMANDO.md Sections 8.1–8.10) on port 8080, per Section 25.3 — so every later Track B step runs against real HTTP with zero backend.

**Prompt to Claude Code / Antigravity:** Use template 0.7 with `{MOCK_NAME}=mock-backend`, `{STANDS_IN_FOR}=every Spring Boot endpoint the frontend consumes (COMMANDO.md Sections 8.1–8.10)`, `{ENDPOINTS}=all routes in Sections 8.1–8.10`, `{PORT}=8080 (so nothing changes at swap time except which server listens)`, `{TOOLING}=json-server with custom routes or a tiny Express app (Section 25.3)`, `{SWAP_STEP}=B-SWAP-1`, appending:
```
Behavioral requirements (all shapes verbatim from Section 8):
1. Auth: POST /api/auth/login and /api/auth/verify-otp return the exact Section 8.1
   example tokens (accessToken "eyJhbGciOiJIUzI1NiJ9...", refreshToken, tokenType
   "Bearer", userId 1, email, role ROLE_AUDITOR, firstName, lastName) so the JWT
   interceptor, refresh-rotation UX, and ProtectedRoute are exercisable. /register
   returns the Section 8.1 201 example; verify-otp accepts ONLY the fixed code 482913
   (any other code → the 400 attempt error). /refresh returns a rotated token pair.
   Protected routes: missing Authorization → 401; the literal token "expired-token" →
   401 (so the refresh-retry flow can be driven deliberately); any other Bearer → 200.
   Special identities: admin@carbontrace.dev logs in with role ROLE_ADMIN (for
   AdminRoute testing); unverified@acme.com → the 400 "Email not verified" example.
2. Upload flow: POST /api/shipments/upload-url returns the Section 8.4 shape with an
   uploadUrl pointing BACK AT THE MOCK (e.g. http://localhost:8080/mock-s3/{s3Key});
   the mock accepts ANY PUT on that path with 200 — the full presigned-PUT upload UX
   with zero AWS. POST /api/shipments then returns the Section 8.4 201
   ShipmentResponseDto in status NEEDS_REVIEW with extracted example fields and a
   fieldConfidence map containing LOW and MEDIUM values (so review-form highlighting
   is exercisable).
3. Error cases, each in the exact ApiResponse.error shape: 401 (bad/expired token),
   400 (validation; "Email not verified"), 403 (auditor on admin routes), 404 (unknown
   ids); the CALCULATED example shipment id 7 rejects PUT /review with the 400
   "already calculated"; the designated shipment id 502 returns 502 "AI service
   unavailable — please try again" on POST /calculate (so the AI-unavailable UI state
   is testable).
4. Paginated lists (vendors, shipments, marketplace credits, purchases, admin users)
   return PagedResponse shapes with >= 12 rows so Pagination is exercisable, including
   the Section 8.6 example listing verbatim. GET /api/shipments/map returns 3–4 points
   derived from the Section 8.4 review example route (Shenzhen→Rotterdam) and other
   Section 8 coordinates. GET /api/analytics/dashboard returns the Section 8.9 example
   verbatim. POST /api/shipments/{id}/calculate (non-502 ids) returns the Section 8.4
   calculate example.
5. Purchases: POST /api/purchases returns the Section 8.7 201 example; when
   maxBudgetUsd <= 20 it instead returns the ApiResponse.error carrying the
   Section 8.11 NO_PURCHASE reasoning text (so the UI's no-purchase path is testable).
```

**Expected Output:** `mocks/mock-backend/` (server + README with run command, port 8080, "MOCK — replaced in B-SWAP-1" label). No frontend code touched.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=STEP B002`, `{STEP_SCOPE}=mock-backend stand-in`, extra:
```
13. Every served body diffs clean against its Section 8 example; camelCase keys;
    ApiResponse/PagedResponse wrappers exact; no invented fields.
14. The mock is standalone Node tooling per Section 25.3 — never imported by frontend
    code, no dependency on backend/.
15. README + "MOCK — replaced in B-SWAP-1" labels present per Section 25.4.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B002`, `{NEGATIVE_CASES}=no token on a protected route → 401; auditor token on /api/admin/users → 403; unknown vendor id → 404; verify-otp with a wrong code → 400`, `{MOCKING_STRATEGY}=none — this step IS the mock; curl it directly`.

**Manual Testing:** Start it on :8080; curl login, credits, dashboard, upload-url; diff each body against its COMMANDO.md Section 8 example (identical); PUT a file to the returned mock uploadUrl → 200.

**Success Criteria:** [ ] Every Section 8.1–8.10 route answers with verbatim shapes; error + pagination cases live; README + labels in place.

**Next Step:** STEP B003.

---

### STEP B003 — Auth Plumbing + Common Components

**Objective:** AuthContext, axios interceptor with refresh-retry, route guards, common components — exercised against mocks/mock-backend.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP B003 ONLY.
The Vite scaffold, global.css tokens, and env files exist from STEP B001. Implement per
Sections 6 and 12:
- src/api/axiosConfig.js: baseURL from VITE_API_BASE_URL; request interceptor attaches
  Bearer token; response interceptor on 401 calls /api/auth/refresh ONCE (refresh token
  from localStorage), retries the original request, logs out on failure.
- src/context/AuthContext.jsx: user, accessToken (memory only), role, login(), logout(),
  isAuthenticated; refresh token in localStorage per Section 10.
- components/common: Navbar, Sidebar, ProtectedRoute (+AdminRoute variant),
  LoadingSpinner, ErrorMessage, EmptyState, Pagination — each with its own CSS Module.
- App.jsx: router skeleton with placeholder-free route table pointing at pages created
  in later steps ONLY where they exist — for now route /login to a minimal LoginPage
  shell created in STEP B004; keep App limited to layout + guards without dead links.
npm run dev must start clean. Do not build feature pages yet.
```

**Expected Output:** Created: axios config, AuthContext, 7 common components + CSS Modules. Modified: `App.jsx`.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=STEP B003`, `{STEP_SCOPE}=frontend foundation`, extra:
```
13. Access token never written to localStorage; refresh token is; interceptor retries
    exactly once then logs out.
14. One CSS Module per component; tokens centralized in global.css; PascalCase files.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B003`, `{NEGATIVE_CASES}=401 from the mock (send the literal token "expired-token") triggers refresh+retry; refresh failure clears state and redirects to /login`, `{MOCKING_STRATEGY}=mocks/mock-backend on :8080 (STEP B002); script section documents npm run dev + build checks plus browser-console fetches instead of curl`.

**Manual Testing (against mocks/mock-backend):** `npm run dev` → app renders shell at :5173; visiting a guarded route unauthenticated redirects to /login; from the browser console, drive the axios instance with the mock's Section 8.1 example tokens: a request sent with "expired-token" gets 401 → the interceptor calls /api/auth/refresh (mock returns a rotated pair) → the retried request succeeds; `npm run build` succeeds; no console errors.

**Success Criteria:** [ ] Interceptor refresh-retry proven against the mock; guards redirect; builds.

**Next Step:** STEP B004.

---

### STEP B004 — Auth Pages

**Objective:** Login, Register, VerifyOtp (countdown + resend), ForgotPassword (2-step).

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP B004 ONLY.
Build per Section 12 with React Hook Form validation mirroring backend rules
(password 8+ with upper/lower/digit, email format, 6-digit OTP):
- components/auth: LoginForm, RegisterForm, OtpVerifyForm (6-digit input, countdown to
  the 10-minute expiry, Resend OTP button), ResetPasswordForm.
- pages: LoginPage, RegisterPage (role radio Auditor/Admin, confirmPassword match),
  VerifyOtpPage (email prefilled via router state), ForgotPasswordPage (step 1 email →
  step 2 OTP + new password).
Flows: register success → VerifyOtpPage; verify success → store tokens (auto-login) →
dashboard route (stub redirect to '/' until STEP B006); login stores tokens and
redirects. All API errors shown via ErrorMessage; loading states everywhere; CSS
Modules per component. Wire routes in App.jsx. npm run dev + build must pass.
```

**Expected Output:** Created: 4 auth components + 4 pages + CSS Modules. Modified: `App.jsx`.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=STEP B004`, `{STEP_SCOPE}=auth pages`, extra:
```
13. Client validation mirrors Section 9 exactly; server error messages surfaced verbatim.
14. Auto-login after OTP verification works (tokens from verify-otp response used).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B004`, `{NEGATIVE_CASES}=wrong OTP shows attempt error; unverified login shows "Email not verified"; expired countdown disables submit and prompts resend`, `{MOCKING_STRATEGY}=mocks/mock-backend (STEP B002): fixed OTP 482913; unverified@acme.com triggers the "Email not verified" 400; admin@carbontrace.dev logs in as ROLE_ADMIN`.

**Manual Testing (against mocks/mock-backend):** Full browser flow: register (mock returns the Section 8.1 201 with otpExpiresInMinutes 10) → VerifyOtpPage → enter the mock's fixed OTP 482913 → mock returns the Section 8.1 example tokens → auto-login redirect; any other code → the attempt error renders; login as unverified@acme.com → "Email not verified" surfaced; forgot-password round trip with code 482913; countdown + Resend UI work.

**Success Criteria:** [ ] All four auth flows work in the browser against mock-backend (real backend comes at B-SWAP-1).

**Next Step:** STEP B005.

---

### STEP B005 — Vendors, Upload Flow, Shipments List/Detail + Review + Calculate

**Objective:** The core auditor working surface: vendor management, direct-to-S3 upload, shipment list, and the review/calculate detail page.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP B005 ONLY.
Per Section 12:
1. VendorsPage + components/vendor (VendorForm, VendorList): table with search +
   Pagination, Add Vendor modal/form; edit + toggle-active controls rendered ONLY for
   role ROLE_ADMIN.
2. UploadShipmentPage + UploadDropzone: vendor select; PDF-only dropzone (client-side
   type + 10MB checks). Flow: POST upload-url → PUT the file to S3 with a BARE axios/
   fetch call (NO Authorization header, Content-Type application/pdf) with a progress
   indicator → POST /api/shipments → show result → navigate to ShipmentDetailPage.
   S3 PUT failure shows a retry option. (During Phase B1 the uploadUrl returned by
   mocks/mock-backend points back at the mock, which accepts any PUT — the client
   code is identical either way.)
3. ShipmentsPage + ShipmentList/ShipmentCard: status + vendor filters, pagination,
   status badges for all five statuses, row click → detail.
4. ShipmentDetailPage + ExtractionReviewForm + ShipmentDetail: every extractable field
   editable; fields whose confidence is LOW or MEDIUM get an amber border + tooltip
   "AI extracted — please verify"; "View original PDF" link via document-url; Save
   Review → PUT review. After REVIEWED: "Calculate Emissions" button → POST calculate;
   on 502 show the "AI service unavailable" error state (exercise it via the mock's
   designated case — shipment id 502; the real 502 path is re-verified after B-SWAP-1).
   After CALCULATED: emissions summary + formula string + a disabled-until-integration
   note is NOT allowed — instead render the real "Offset this shipment" button with optional
   max-budget input wired to POST /api/purchases (the mock serves both the Section 8.7
   PURCHASE result and the NO_PURCHASE explanation for maxBudgetUsd <= 20); purchase
   history section for this shipment; results open in
   PurchaseResultModal labeled "SIMULATED PURCHASE — no real transaction occurred".
Loading/error/empty states on every page; numbers formatted per Section 12 (kgCO₂e
thousands separators, USD 2dp, tonnes 3dp). npm run build must pass.
```

**Expected Output:** Created: vendor/shipment/marketplace-modal components + 4 pages + CSS Modules. Modified: routes.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=STEP B005`, `{STEP_SCOPE}=vendors + upload + shipments UI`, extra:
```
13. The S3 PUT carries NO Authorization header (inspect network tab / code).
14. Review form supports the FAILED → manual-entry path with a clear banner.
15. Number formatting rules applied everywhere; admin-only controls hidden for auditors.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B005`, `{NEGATIVE_CASES}=non-PDF drop rejected client-side; 11MB file rejected; S3 PUT failure → retry UI (stop the mock mid-upload); calculate on shipment 502 → 502 banner; review of the CALCULATED shipment id 7 blocked by the mock's 400 and surfaced`, `{MOCKING_STRATEGY}=mocks/mock-backend only — fake presigned PUT (zero AWS) and the designated shipment-502 case for the 502 banner`.

**Manual Testing (against mocks/mock-backend):** Browser: add vendor (mock returns the Section 8.3 201) → upload any local PDF (dropzone → upload-url → PUT to the mock's fake presigned URL succeeds with progress → POST /api/shipments) → shipment appears NEEDS_REVIEW with the mock's canned extracted fields; LOW/MEDIUM-confidence fields render amber with the tooltip → edit + Save Review → Calculate → mock returns the Section 8.4 calculate example (totalEmissionsKgco2e 4218.750, distanceKm 20430.00, distanceSource COMPUTED, status CALCULATED) → summary renders. Open the mock's shipment id 502 → Calculate → the 502 "AI service unavailable — please try again" state renders. "Offset this shipment" with budget 200 → PurchaseResultModal shows the Section 8.7 example (SIM-9F3A2C71) labeled SIMULATED; budget 20 → the NO_PURCHASE explanation renders. Check in DevTools that the presigned PUT carries no Authorization header.

**Success Criteria:** [ ] Upload→review→calculate→offset chain fully works in the browser against the mock; error states correct.

**Next Step:** STEP B006.

---

### STEP B006 — Dashboard, Map, Goals, Marketplace, Purchases, Admin Pages

**Objective:** All remaining pages: analytics dashboard, leaflet map, goals, marketplace, purchase history, admin tabs.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP B006 ONLY.
Per Section 12:
1. DashboardPage + StatCards, EmissionsTrendChart (Recharts line/bar on
   monthlyEmissions), ModeBreakdownChart (Recharts pie), GoalProgress bars,
   per-vendor table, quick actions (Upload Freight Bill / View Map / Marketplace).
2. EmissionsMapPage + EmissionsMap: react-leaflet with OpenStreetMap tiles (no API
   key); per CALCULATED shipment: origin+destination markers, polyline between them,
   color/weight scaled by emissions, popup with route/mode/kgCO₂e. Handle the leaflet
   default-icon asset fix for Vite.
3. GoalsPage: list with progress bars + create/edit form (validation mirrors rules).
4. MarketplacePage + ListingCard/ListingList: grid with standard badges, price/tonne,
   available tonnes, vintage; filters projectType + max price; PERSISTENT banner
   "Simulated marketplace — demo data".
5. PurchasesPage: table (date, shipment, project, tonnes, cost, SIM reference) with
   expandable rows showing agent reasoning.
6. AdminPage (AdminRoute-guarded) with three tabs: UserManagement (role filter +
   toggle), EmissionFactorManagement (table + create/edit), MarketplaceManagement
   (sellers + listings incl. restock).
Finalize Navbar/Sidebar navigation and default redirects. Loading/error/empty states
everywhere. npm run build passes.
```

**Expected Output:** Created: dashboard/emissions/marketplace/admin components + 6 pages + CSS Modules. Frontend feature-complete.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=STEP B006`, `{STEP_SCOPE}=remaining pages`, extra:
```
13. Simulated-marketplace banner is persistent on marketplace surfaces; purchase modal
    labels SIMULATED.
14. Map renders only /api/shipments/map data; polyline styling scales with emissions.
15. Admin tabs unreachable for auditors (route guard + hidden nav).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B006`, `{NEGATIVE_CASES}=empty dashboard renders zeros; map with no data shows EmptyState; auditor deep-links /admin → redirected`, `{MOCKING_STRATEGY}=mocks/mock-backend canned data — dashboard serves the Section 8.9 example verbatim; /api/shipments/map serves the mock's 3–4 canned points; credits serve the Section 8.6 listing set`.

**Manual Testing (against mocks/mock-backend):** Browser sweep of all pages in both roles (auditor login + admin@carbontrace.dev); charts match the Section 8.9 example values (totalEmissionsKgco2e 187430.250, six monthly buckets oldest-first); map polylines render between the mock's canned coordinates (the Shenzhen→Rotterdam route among them); marketplace grid shows the Section 8.6 example listing; Phase B1 checklist review.

**Success Criteria:** [ ] All 14 pages functional against the mock; Phase B1 checklist green (run template 0.9).

**Next Step:** B-SWAP-1 — wait for Track A to tick both rows Track B consumes in the Integration Readiness Checklist; run template 0.9 while waiting.

---

### B-SWAP-1 — Swap to the Real Backend + Real S3 (retires mock-backend)

**Objective:** Point the browser at the real Spring Boot backend and the real S3 presigned flow; re-run the entire Track B manual test list against the real stack; delete `mocks/mock-backend/`.

**Precondition:** Track A has ticked BOTH rows Track B consumes in the Integration Readiness Checklist: "Real backend auth + CRUD" (Phases A1–A5) and "Real S3 presigned upload flow" (STEP A016 + Manual Setup C).

**Prompt to Claude Code / Antigravity:** Use template 0.8 with `{SWAP_ID}=B-SWAP-1`, `{MOCK}=mocks/mock-backend`, `{REAL_DEPENDENCY}=the real Spring Boot backend on :8080 plus the real S3 bucket via presigned URLs`, `{CONFIG_CHANGE}=none — VITE_API_BASE_URL stays http://localhost:8080/api; stop mock-backend and start the real backend`, `{ORIGINAL_TESTS}=every testing/track-b/step-B0xx script plus the Manual Testing lists of STEPS B003–B006`, plus:
```
Additional required checks for this swap:
1. Auth for real: register a fresh user (console OTP fallback or real SMTP), verify,
   login — real JWTs now differ from the mock's fixed example tokens; the interceptor
   refresh-retry must work against real rotation.
2. The REAL browser→S3 presigned upload: drag-drop a PDF; in DevTools confirm the PUT
   goes to the s3.amazonaws.com uploadUrl with NO Authorization header and succeeds
   (bucket CORS per Manual Setup C); the object appears in the S3 console;
   document-url opens the real PDF.
3. Expected partial behavior: extraction, calculation, and purchase results now depend
   on Track A's swap progress — before A-SWAP-1..3 complete, creation returns the
   STEP A017 stub (NEEDS_REVIEW, null fields) or the FAILED/502 paths. That is
   EXPECTED here; the full happy paths are verified jointly in STEPS I001–I002.
4. Triage every failure with template 0.10 — fix only the side violating Section 8.
5. Delete mocks/mock-backend (directory + README entry); verify no reference remains.
```

**Expected Output:** No feature changes — configuration-only swap + Section 8-justified fixes. Deleted: `mocks/mock-backend/`.

**Verification Prompt:** Template 0.3B, `{STEP_ID}=B-SWAP-1`, `{STEP_SCOPE}=real-backend swap`, extra:
```
13. Zero references to mocks/mock-backend remain; VITE_API_BASE_URL unchanged.
14. The real S3 PUT carries no Authorization header and succeeds (CORS proven).
15. No business logic changed by the swap.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=B-SWAP-1`, `{NEGATIVE_CASES}=login with wrong password (real 401); auditor deep-links /admin (real 403 + redirect); expired presigned URL → S3 403 → retry UI`, `{MOCKING_STRATEGY}=none — fully real backend + S3`.

**Manual Testing:** Re-run the full B003–B006 manual test lists against the real stack, noting the expected differences from the mock (real tokens, real data instead of canned examples, extraction behavior per Track A's swap progress).

**Success Criteria:** [ ] Full frontend works against the real backend incl. the real S3 upload; mock-backend deleted.

**Next Step:** When ALL five swap steps are complete, merge `track-b` to `main` and join the SHARED INTEGRATION PHASE at STEP I001.

---

# ═══ TRACK C — AI SERVICE (Developer C starts here on day one) ═══

Track C owns `ai-service/` and `testing/track-c/`. It builds `mocks/mock-marketplace/` and `mocks/sample-pdfs/` (STEP C002) so the extraction pipeline and the purchase agent never wait on the backend or AWS. Work on branch `track-c`; verification prompts use template 0.3C. Nothing in Phase C1 depends on Track A or Track B until C-SWAP-1.

# PHASE C1 — AI SERVICE (STEPS C001–C005 + C-SWAP-1)

**Purpose:** Build the AI compute service: deterministic calculator, extraction pipeline (tested against sample PDFs served over plain HTTP), and the autonomous purchase agent (tested against the mock marketplace). The backend-side wiring of /extract, /calculate, and /agent/purchase is Track A's work (A-SWAP-1/2/3).

**Prerequisites:** Python 3.11 installed (`python3.11 --version`); MANUAL SETUP D (Gemini key) before STEP C001's key wiring (calculator in STEP C003 works without it). NOTHING from Track A or Track B.

**Deliverables:** `ai-service/` with /health, /calculate, /extract, /agent/purchase — every endpoint proven with mock-based tests; `mocks/mock-marketplace/` + `mocks/sample-pdfs/`.

**Dependencies:** None outside this track — the Section 8.6 marketplace shapes and the S3-style document URLs come from the STEP C002 mocks.

**Phase completion checklist:**
- [ ] /calculate matches hand-computed haversine values
- [ ] /extract returns nulls for missing fields, confidence flags, 422 for image-only PDFs (sample PDFs over plain HTTP — httpx treats them identically to presigned URLs)
- [ ] Agent purchases end-to-end against mocks/mock-marketplace, incl. NO_PURCHASE and tie-breaking cases
- [ ] Ran template 0.9 contract-consistency check — all endpoints PASS
- [ ] My mocks (if any) still match COMMANDO.md Section 8 verbatim

---

### 🔧 MANUAL SETUP D — Google Gemini API Key (do this YOURSELF, ~5 min)

Required before STEP C001 completes (the /extract and agent steps need it; /calculate does not).

**Sub-steps:**
1. Go to https://ai.google.dev → **Get API key** → sign in with a Google account.
2. Google AI Studio → **Create API key** (new or existing Google Cloud project). Free tier is sufficient for `gemini-1.5-flash`.
3. Copy the key. Create `ai-service/.env` with `GEMINI_API_KEY=<key>`, `SPRING_BOOT_URL=http://localhost:8080`, `LOG_LEVEL=INFO`.

**Verification of manual work:**
- [ ] `curl -s "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$GEMINI_API_KEY" -H 'Content-Type: application/json' -d '{"contents":[{"parts":[{"text":"ping"}]}]}'` returns a JSON candidate (not a 4xx auth error).
- [ ] `ai-service/.env` is gitignored (`git check-ignore ai-service/.env` succeeds).

---

### STEP C001 — FastAPI Setup

**Objective:** Scaffold the service: app, config, all Pydantic schemas, /health, requirements.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP C001 ONLY.
Create ./ai-service exactly per Section 6's flat layout:
- requirements.txt: fastapi 0.115.x, uvicorn, langchain, langchain-google-genai,
  pydantic v2, httpx, pdfplumber, python-dotenv (nothing else).
- config.py: load GEMINI_API_KEY, SPRING_BOOT_URL, LOG_LEVEL from env/.env; configure
  logging (INFO default; every request path + outcome logged; presigned URLs truncated
  at the query string before logging).
- schemas.py: ALL Pydantic models copied VERBATIM from Section 13 (ExtractRequest/
  Response, CalculateRequest/Response, AgentPurchaseRequest/Response).
- main.py: FastAPI app, router registration placeholders NOT allowed — register only
  what exists: add GET /health returning {"status": "ok"}; routers are added in later
  steps.
- Empty packages extraction/, agent/, clients/, routers/ with __init__.py only.
- ai-service/.gitignore covering .env and .venv/ — the repo-root .gitignore is
  Track A's (STEP A001) and only reaches this branch at the integration merge, so
  Track C must not depend on it (Manual Setup D's git check-ignore verification
  relies on this file).
Create a venv-based run instruction in ai-service/README-dev.md
(python3.11 -m venv .venv; pip install -r requirements.txt; uvicorn main:app --reload).
Do NOT implement calculator, extraction, or agent code. Verify uvicorn starts and
/health responds.
```

**Expected Output:** Created: `ai-service/` scaffold, schemas, /health.

**Verification Prompt:** Template 0.3C, `{STEP_ID}=STEP C001`, `{STEP_SCOPE}=FastAPI scaffold`, extra:
```
13. schemas.py matches Section 13 verbatim (types, defaults, snake_case).
14. Flat structure — no service layers, DI containers, or repositories (Section 6 rule).
15. Logging truncates URLs; GEMINI key never logged.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=C001`, `{NEGATIVE_CASES}=missing GEMINI_API_KEY logs a clear warning at startup but /health still works`, `{MOCKING_STRATEGY}=none`.

**Manual Testing:** `cd ai-service && python3.11 -m venv .venv && . .venv/bin/activate && pip install -r requirements.txt && uvicorn main:app --reload` → `curl localhost:8000/health` → `{"status":"ok"}`.

**Success Criteria:** [ ] Service boots; schemas verbatim; nothing extra.

**Next Step:** STEP C002.

---

### STEP C002 — Build mocks/mock-marketplace + mocks/sample-pdfs (Template 0.7)

**Objective:** Two local stand-ins per COMMANDO.md Section 25.3: the Spring Boot public marketplace GETs (for the agent) and S3-presigned-style document URLs (for /extract testing).

**Prompt to Claude Code / Antigravity (run template 0.7 twice):**

First, `{MOCK_NAME}=mock-marketplace`, `{STANDS_IN_FOR}=Spring Boot's public read-only marketplace GETs (COMMANDO.md Section 8.6)`, `{ENDPOINTS}=GET /api/marketplace/credits (projectType/maxPricePerTonne/minAvailableTonnes filters + paging), GET /api/marketplace/credits/{id}, GET /api/marketplace/sellers/{id}`, `{PORT}=8080 (locally for Track C)`, `{TOOLING}=a ~50-line standalone Python script or tiny Node server (Section 25.3)`, `{SWAP_STEP}=C-SWAP-1`, appending:
```
Seed 6–8 listings in the exact Section 8.6 shape (ApiResponse + PagedResponse wrapper,
seller fields embedded) covering the agent's decision cases: the Section 8.6 example
listing verbatim (id 3, GOLD_STANDARD, $14.50/t, 5200.000 t available); a CHEAPER VCS
listing at $13.75/t with sufficient availability (so standard-beats-price tie-breaking
is provable); an expensive listing; a listing with < 1 tonne availability; listings
with different seller ratings for the final tie-break; and ONE inactive listing that
must never be served as active. Filters must actually filter. Log every request
(method + path) so read-only behavior is verifiable.
```

Second, `{MOCK_NAME}=sample-pdfs`, `{STANDS_IN_FOR}=S3 presigned GET URLs for /extract testing`, `{ENDPOINTS}=static file GETs`, `{PORT}=9000`, `{TOOLING}=3–5 generated freight-bill PDFs + a one-line static server (python3 -m http.server 9000) with a run script (Section 25.3)`, `{SWAP_STEP}=A-SWAP-1 proves the real presigned path; the PDF files may remain afterwards as test fixtures (Section 25.3)`, appending:
```
Generate the PDFs programmatically with a one-off script (text-layer PDFs):
(1) a clean invoice, route Shenzhen→Rotterdam, SEA, 18.5 t (mirrors the Section 8.11
/extract example); (2) a ROAD bill with weight stated in kg, to test unit conversion;
(3) an invoice with missing fields (no invoice number, no fuel type), to prove
nulls-are-never-guessed; (4) an image-only/blank PDF for the 422 path. Document in the
README that httpx downloads these plain-HTTP URLs identically to S3 presigned GET
URLs, so no ai-service code changes at integration time.
```

**Expected Output:** `mocks/mock-marketplace/` and `mocks/sample-pdfs/` (each with README, port, retirement label). No ai-service code touched.

**Verification Prompt:** Template 0.3C, `{STEP_ID}=STEP C002`, `{STEP_SCOPE}=Track C mocks`, extra:
```
13. Listing bodies diff clean against Section 8.6 (wrapper, keys, casing, example
    listing verbatim); the seed covers cheap/expensive, low/high availability,
    standards, ratings, one inactive.
14. Neither mock depends on backend/ or ai-service/ code; neither is imported by
    production code.
15. READMEs + "MOCK — replaced in ..." labels present per Section 25.4.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=C002`, `{NEGATIVE_CASES}=credits filter maxPricePerTonne=10 excludes pricier rows; unknown listing id → 404 in ApiResponse.error shape`, `{MOCKING_STRATEGY}=none — these ARE the mocks; curl them directly`.

**Manual Testing:** Start both; `curl "localhost:8080/api/marketplace/credits?maxPricePerTonne=15"` → filtered Section 8.6-shaped page; `curl -O localhost:9000/<clean-sample>.pdf` downloads.

**Success Criteria:** [ ] Marketplace shapes verbatim + decision-case seed; 4 sample PDFs served over HTTP; READMEs + labels in place.

**Next Step:** STEP C003.

---

### STEP C003 — calculator.py + /calculate Router

**Objective:** 100% deterministic emission math verified against known values. (This is also the C-side half of the end-to-end calculation verification: Track A re-runs these same payloads against the real service at A-SWAP-2.)

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP C003 ONLY.
1. calculator.py per Section 15 exactly:
   - haversine(lat1, lng1, lat2, lng2): great-circle km, Earth radius 6371.0.
   - compute_distance(req): distance_km provided and > 0 → (value, "DOCUMENT");
     else (round(haversine * circuity_factor, 2), "COMPUTED").
   - calculate_emissions(req): total = round(weight * distance * factor, 3); formula
     string exactly like "18.5 t × 20430.00 km × 0.011 kgCO2e/t·km".
   - Guard rails raising ValueError for: weight <= 0, factor <= 0, neither distance nor
     all four coordinates.
2. routers/calculate.py: POST /calculate → thin router calling calculator; translate
   guard-rail failures to HTTPException 422. NO LLM, NO network in this path.
3. Register the router in main.py.
Verify with curl using the Section 8.11 example payload. Type hints everywhere.
```

**Expected Output:** Created: `calculator.py`, `routers/calculate.py`. Modified: `main.py`. Endpoint: /calculate.

**Verification Prompt:** Template 0.3C, `{STEP_ID}=STEP C003`, `{STEP_SCOPE}=deterministic calculator`, extra:
```
13. No imports of langchain/httpx in calculator.py or the calculate router.
14. Shenzhen→Rotterdam with circuity 1.15 and factor 0.011 at 18.5t reproduces the
    Section 8.11 example within rounding (distance ≈ 20430 km level of magnitude —
    assert your own haversine result and document it).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=C003`, `{NEGATIVE_CASES}=weight 0 → 422; factor -1 → 422; no distance + missing one coordinate → 422; distance provided → DOCUMENT source, circuity NOT applied`, `{MOCKING_STRATEGY}=pure curl; include a python -c one-liner cross-check of haversine for a known pair (e.g. London–Paris ≈ 344 km)`.

**Manual Testing:** curl the Section 8.11 payload → response has distance_source COMPUTED, formula string; London(51.5074,-0.1278)–Paris(48.8566,2.3522) haversine ≈ 343–344 km sanity check.

**Success Criteria:** [ ] Math verified against known values; guard rails 422.

**Next Step:** STEP C004. Also tick the "Real /calculate" row in the Integration Readiness Checklist now — Track A's A-SWAP-2 consumes it.

---

### STEP C004 — Extraction Pipeline (pdf_parser + llm_extractor) + /extract Router

**Objective:** Deterministic PDF text extraction → Gemini structured extraction → post-validation; tested with sample freight bills.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP C004 ONLY.
1. extraction/pdf_parser.py: pdfplumber over PDF bytes, page-by-page text joined with
   page markers; if total text < 50 chars raise a typed error mapped to 422 with the
   exact message "Document has no extractable text — OCR is not supported in the MVP;
   enter fields manually". No OCR.
2. extraction/llm_extractor.py: ONE structured-output call to gemini-1.5-flash
   (temperature 0) via langchain-google-genai returning the ExtractResponse-shaped
   schema. The prompt MUST contain the three requirement blocks from Section 14
   verbatim in spirit: never guess (nulls for absent values), normalization rules
   (tonnes, yyyy-MM-dd, mode/fuel enums), city-coordinate exception flagged MEDIUM max,
   per-field HIGH/MEDIUM/LOW, and the overall_confidence derivation rule. One retry on
   transient failure, then raise mapped to 502.
3. Deterministic post-validation per Section 14: lat/lng ranges, 0 < weight < 100000,
   date parseable, enums in range — any failing field set to null with confidence LOW;
   recompute overall_confidence after nulling.
4. routers/extract.py: POST /extract → httpx GET document_url (timeout 30s; 422 if
   download fails) → parser → extractor → post-validation → ExtractResponse. Register
   in main.py. Log path + outcome, never full presigned URLs.
5. Extraction testing uses the sample freight-bill PDFs from mocks/sample-pdfs (STEP
   C002), served at http://localhost:9000/, as document_url values. httpx downloads
   these plain-HTTP URLs EXACTLY as it would S3 presigned GET URLs — a URL is a URL to
   httpx — so NO code changes are needed at integration time; the real presigned path
   is proven by Track A at A-SWAP-1. Do NOT create new sample PDFs here.
Verify with curl using the mocks/sample-pdfs URLs.
```

**Expected Output:** Created: `pdf_parser.py`, `llm_extractor.py`, `routers/extract.py`. Endpoint: /extract. (The sample PDFs already exist in mocks/sample-pdfs from STEP C002.)

**Verification Prompt:** Template 0.3C, `{STEP_ID}=STEP C004`, `{STEP_SCOPE}=extraction pipeline`, extra:
```
13. Deterministic-first: parser runs before any LLM call; temperature 0; single retry.
14. Nulls for absent fields (no guessing); kg→tonnes conversion verified on sample 2.
15. Post-validation nulls out-of-range values with LOW confidence.
16. 422 vs 502 mapping exactly per Sections 8.11/13.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=C004`, `{NEGATIVE_CASES}=blank PDF → 422 exact message; unreachable document_url → 422; (document) Gemini failure path → 502 after one retry`, `{MOCKING_STRATEGY}=mocks/sample-pdfs static server (STEP C002) provides document_url values; real Gemini key from MANUAL SETUP D`.

**Manual Testing:** curl /extract with document_url values from http://localhost:9000/ — the clean sample → fields populated, coordinates MEDIUM, weight 18.5; the kg-weight ROAD sample → tonnes converted; the missing-fields sample → nulls (never guessed); the blank PDF → 422 exact message; check logs truncate URLs.

**Success Criteria:** [ ] All three samples behave per spec; confidence flags sensible.

**Next Step:** STEP C005. Also tick the "Real /extract" row in the Integration Readiness Checklist now — Track A's A-SWAP-1 consumes it.

---

### STEP C005 — Autonomous Purchase Agent + /agent/purchase Router

**Objective:** The LangChain tool-calling agent with the three marketplace GET tools + simulate_payment, tested standalone against mocks/mock-marketplace (STEP C002).

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP C005 ONLY.
1. clients/marketplace_client.py: httpx wrapper (10s timeout per call) for the ONLY
   three allowed Spring Boot endpoints: GET /api/marketplace/credits (with filters,
   size=50), GET /api/marketplace/credits/{id}, GET /api/marketplace/sellers/{id}.
   No other endpoint may ever be called; no writes.
2. agent/tools.py: a per-request tool factory bound to marketplace_base_url producing
   the four tools from Section 16 with those exact signatures: list_credits,
   get_credit_details, get_seller_details (network via the client, returning compact
   JSON strings), and simulate_payment (LOCAL only: reference "SIM-" +
   uuid4().hex[:8].upper(), fake receipt JSON {"status":"PAID",...}).
3. agent/purchase_agent.py: LangChain tool-calling agent (create_tool_calling_agent +
   AgentExecutor or current equivalent), gemini-1.5-flash, temperature 0,
   max_iterations 8, system prompt containing the Section 16 core content verbatim
   (required_tonnes, budget constraint, selection priority: constraints →
   GOLD_STANDARD > VCS > CDM → lower price → higher rating; NO_PURCHASE with
   explanation when nothing fits). Parse the final answer into AgentPurchaseResponse
   via a structured-output step; deterministic consistency checks: total_cost = tonnes
   × price within $0.01 and transaction_reference present when decision is PURCHASE;
   on malformed output downgrade to NO_PURCHASE with an error explanation. One retry
   on tool/LLM failure, then NO_PURCHASE describing the failure.
4. routers/agent.py: POST /agent/purchase per Section 8.11; register in main.py.
Test standalone against mocks/mock-marketplace (STEP C002) running on :8080 —
marketplace_base_url in the request payload points there. The Spring Boot side of this
boundary is Track A's work (A-SWAP-3); nothing outside ai-service/ is touched here.
```

**Expected Output:** Created: `marketplace_client.py`, `tools.py`, `purchase_agent.py`, `routers/agent.py`. Endpoint: /agent/purchase. Nothing outside ai-service/ touched.

**Verification Prompt:** Template 0.3C, `{STEP_ID}=STEP C005`, `{STEP_SCOPE}=purchase agent`, extra:
```
13. Agent tools can ONLY reach the three read-only GETs; simulate_payment is local.
14. Consistency verification + malformed-output downgrade implemented in code, not
    left to the LLM.
15. System prompt encodes the exact selection priority and NO_PURCHASE rule.
16. temperature 0, max_iterations 8, per-tool timeout 10s, read timeout on Spring
    Boot's side 120s (Section 18 Flow 6).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=C005`, `{NEGATIVE_CASES}=required_tonnes larger than every listing → NO_PURCHASE with explanation; max_budget_usd=5 → NO_PURCHASE citing cheapest viable cost; mock-marketplace stopped → NO_PURCHASE describing tool failure`, `{MOCKING_STRATEGY}=real Gemini + mocks/mock-marketplace on :8080; curl /agent/purchase directly with the Section 8.11 payload (marketplace_base_url=http://localhost:8080 → the mock)`.

**Manual Testing:**
- With mocks/mock-marketplace running: `curl -X POST localhost:8000/agent/purchase -d '{"shipment_id":7,"required_tonnes":4.219,"max_budget_usd":200.0,"marketplace_base_url":"http://localhost:8080"}'` → decision PURCHASE, SIM- reference, coherent reasoning mentioning compared listings.
- Tie-breaking against the C002 seed: the agent must pick the GOLD_STANDARD listing at $14.50/t over the cheaper $13.75/t VCS listing (standard outranks price); the inactive listing and the <1 t availability listing must never be chosen.
- Budget 20 → NO_PURCHASE with cost explanation. Check the mock's request log: only GETs — no POST/PUT ever arrives from FastAPI.

**Success Criteria:** [ ] Agent selects per the priority order against the mock seed; all failure modes → NO_PURCHASE; read-only confirmed. Phase C1 checklist green (run template 0.9).

**Next Step:** C-SWAP-1 — wait for Track A to tick the "Real marketplace GETs" row in the Integration Readiness Checklist; run template 0.9 while waiting.

---

### C-SWAP-1 — Swap the Agent onto the Real Marketplace (retires mock-marketplace)

**Objective:** Repoint the agent's marketplace at the real Spring Boot marketplace and re-run the same agent test cases; delete `mocks/mock-marketplace/`.

**Precondition:** Track A has ticked "Real marketplace GETs" in the Integration Readiness Checklist (STEP A025 done — real listings seeded).

**Prompt to Claude Code / Antigravity:** Use template 0.8 with `{SWAP_ID}=C-SWAP-1`, `{MOCK}=mocks/mock-marketplace`, `{REAL_DEPENDENCY}=Spring Boot's public read-only marketplace GETs (Track A, STEP A025)`, `{CONFIG_CHANGE}=none in code — marketplace_base_url/SPRING_BOOT_URL stays http://localhost:8080; stop mock-marketplace and let the real backend answer`, `{ORIGINAL_TESTS}=testing/track-c/step-C005-tests.sh`, plus:
```
Re-run every STEP C005 agent case against the real marketplace: the budget-200 PURCHASE
case, the budget-20 NO_PURCHASE case, and the tie-breaking checks as far as the real
STEP A025 seed covers them (its 8 listings span the same decision cases: cheap/
expensive, low/high availability, different verification standards). Diff the agent's
tool observations against the recorded mock behavior; any contract mismatch is a
Section 8 discrepancy (template 0.10) — never adapt silently. Delete
mocks/mock-marketplace (directory + README entry). Then tick the
"Real /agent/purchase" row in the Integration Readiness Checklist so Track A can run
A-SWAP-3.
```

**Expected Output:** No code changes beyond Section 8-justified fixes. Deleted: `mocks/mock-marketplace/`.

**Verification Prompt:** Template 0.3C, `{STEP_ID}=C-SWAP-1`, `{STEP_SCOPE}=marketplace swap`, extra:
```
13. Zero references to mocks/mock-marketplace remain; SPRING_BOOT_URL unchanged.
14. The agent still calls ONLY the three read-only GETs; no writes observed in the
    backend's logs.
15. No business logic changed by the swap.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=C-SWAP-1`, `{NEGATIVE_CASES}=required_tonnes larger than every real listing → NO_PURCHASE; backend stopped → NO_PURCHASE describing tool failure`, `{MOCKING_STRATEGY}=none — real Gemini + real Spring Boot marketplace`.

**Manual Testing:** Same curl cases as STEP C005 with the real backend answering on :8080; reasoning references the real seeded listings; Spring Boot logs show only GET requests from FastAPI.

**Success Criteria:** [ ] Same agent behavior against the real marketplace; mock-marketplace deleted; "Real /agent/purchase" row ticked.

**Next Step:** When ALL five swap steps are complete, merge `track-c` to `main` and join the SHARED INTEGRATION PHASE at STEP I001.

---

# SHARED INTEGRATION PHASE (STEPS I001–I003) — ALL THREE MEMBERS TOGETHER

**Purpose:** The joint phase all three members run TOGETHER on `main` after ALL swap steps pass: full purchase flow, complete role/flow smoke test, and edge-case hardening — everything real, nothing mocked.

**Prerequisites:** `track-a`, `track-b`, `track-c` merged to `main`; ALL five swap steps (A-SWAP-1..3, B-SWAP-1, C-SWAP-1) complete; all three processes + RDS + S3 + Gemini + (optionally) SMTP live. The `mocks/` directory no longer exists — verify with `git status` (clean; no pending mock deletions) and `find . -maxdepth 2 -type d -name "mocks*"` (no output).

### Integration Readiness Checklist

The handoff board for the whole team (COMMANDO.md Section 25.5). A providing track ticks a row the moment the real dependency is usable; the consuming track then runs its swap step. Until a row is ticked, the consumer keeps working on mocks — nobody is ever blocked.

| Boundary (real dependency) | Providing track | Ready after | Consuming swap step | Ready? |
|---|---|---|---|---|
| Real backend auth + CRUD (Sections 8.1–8.10) | A | Phases A1–A5 (STEP A027) | B-SWAP-1 | [ ] |
| Real S3 presigned upload flow (bucket CORS per Manual Setup C) | A | STEP A016 + Manual Setup C | B-SWAP-1 | [ ] |
| Real marketplace GETs (Section 8.6, seeded) | A | STEP A025 | C-SWAP-1 | [ ] |
| Real /extract (Section 8.11) | C | STEP C004 | A-SWAP-1 | [ ] |
| Real /calculate (Section 8.11) | C | STEP C003 | A-SWAP-2 | [ ] |
| Real /agent/purchase (agent on the real marketplace) | C | STEP C005 + C-SWAP-1 | A-SWAP-3 | [ ] |

**Deliverables:** Verified purchase E2E incl. NO_PURCHASE and 409 paths; smoke-test and edge-case reports; only bug-fix diffs.

**Dependencies:** Everything, from all three tracks.

**Phase completion checklist:**
- [ ] Real agent purchase decrements inventory and shows reasoning + SIM ref in the UI
- [ ] Budget-too-low and stale-listing (409) paths verified
- [ ] All-roles smoke test passes
- [ ] All six STEP I003 edge cases pass

---

### STEP I001 — Full Purchase Flow End-to-End

**Objective:** Dashboard → shipment → offset → agent → simulated payment → transactional commit → UI, including the failure paths.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and execute STEP I001 ONLY.
1. Precondition check: ALL five swap steps are complete and mocks/ is gone — verify
   with git status (clean) and `find . -maxdepth 2 -type d -name "mocks*"` (no
   output). The real agent has been the only purchase path since A-SWAP-3.
2. With the full stack running, verify:
   a. UI: open a CALCULATED shipment → "Offset this shipment" (budget 200) → modal
      shows agent reasoning + SIM reference labeled SIMULATED; psql: listing
      available_tonnes decremented by exactly tonnesPurchased; shipment.offset_tonnes
      incremented; offset_purchases row with snapshots.
   b. Budget-too-low: repeat on another shipment with budget 1 → UI shows the agent's
      NO_PURCHASE explanation; NO row written.
   c. 409 stale-listing: start a purchase, and before it commits change the chosen
      listing's price via the admin endpoint by more than $0.01 (or pre-change it and
      craft the race per the report) → 409 surfaced in the UI; no row; inventory
      unchanged. Document the exact technique used to force it.
   d. Dashboard/purchases page reflect the new purchase (spend, net emissions).
3. Fix only integration bugs, minimal diffs, all listed in
   testing/shared/step-I001-purchase-report.md.
```

**Expected Output:** Modified: bug fixes only. Created: purchase report.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP I001`, `{STEP_SCOPE}=purchase E2E`, extra:
```
13. Zero references to mocks/, mock-ai, mock-backend, or mock-marketplace remain
    anywhere in code, config, or test scripts (this guide and COMMANDO.md aside).
14. NO_PURCHASE and 409 leave the database bit-for-bit unchanged (row counts + tonnes).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=I001`, `{NEGATIVE_CASES}=fully-offset shipment re-purchase → 400; concurrent purchases against a nearly-empty listing → one succeeds, one 409`, `{MOCKING_STRATEGY}=fully real stack`.

**Manual Testing:** As in the prompt; watch FastAPI logs for the agent's tool calls (only GETs); confirm `SELECT SUM(available_tonnes)` arithmetic.

**Success Criteria:** [ ] All four scenarios pass; mocks/ verified gone.

**Next Step:** STEP I002.

---

### STEP I002 — Full Smoke Test of All Roles and Flows

**Objective:** One continuous pass over every user-facing flow; fix CORS/auth/S3/data-flow issues only.

**Prerequisite:** Complete MANUAL SETUP B (Gmail) now if you deferred it — this step verifies real OTP mail.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and execute STEP I002 ONLY. No new features.
Precondition: all swap steps complete; mocks/ directory removed — verify with git
status and a find command before starting.
Run the exact smoke sequence below in the browser and record each result in
testing/shared/step-I002-smoke-report.md:
register+OTP (real email if SMTP configured) → add vendor → upload PDF → review
(correct at least one low-confidence field) → calculate → emissions map → dashboard →
create goal → purchase offset → purchases history (expand reasoning) → admin: user
toggle → admin: emission factor edit → admin: marketplace restock.
Fix ONLY defects found (CORS, auth, S3-CORS, data-flow), minimal diffs, listed in the
report with root causes.
```

**Expected Output:** Smoke report; bug-fix diffs only.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP I002`, `{STEP_SCOPE}=smoke fixes`, extra:
```
13. Every fix maps to a report entry; no refactors; report covers all 13 flow items.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=I002`, `{NEGATIVE_CASES}=covered by the flow itself`, `{MOCKING_STRATEGY}=real everything incl. SMTP`.

**Manual Testing:** The sequence IS the manual test; verify OTP arrives by email; watch browser console for CORS errors (must be none).

**Success Criteria:** [ ] All 13 items pass; zero console CORS/auth errors.

**Next Step:** STEP I003.

---

### STEP I003 — Edge Cases

**Objective:** Verify the six Section 20 STEP I003 edge cases; harden only where they fail.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and execute STEP I003 ONLY. Precondition: all swap steps complete;
mocks/ directory removed — verify with git status and a find command before starting.
Verify each edge case and record in testing/shared/step-I003-edge-report.md:
1. Expired presigned URL: wait >10 min after upload-url, PUT → S3 403; UI shows retry.
2. Image-only PDF: upload the blank sample → shipment FAILED with the OCR message →
   manual review → calculate succeeds.
3. Duplicate offset on a fully-offset shipment → 400 "already fully offset".
4. Expired JWT refresh: shrink JWT_EXPIRATION to 10s locally, confirm the interceptor
   refreshes once and retries; revoke the refresh token in DB → logout occurs. Restore
   the config afterwards.
5. OTP attempt limit: 5 wrong codes → 6th correct code still rejected; resend works.
6. Inactive vendor booking: deactivate a vendor → shipment creation → 400.
Fix only real failures, minimal diffs, all listed.
```

**Expected Output:** Edge report; targeted fixes only.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP I003`, `{STEP_SCOPE}=edge-case fixes`, extra:
```
13. Temporary config changes (short JWT expiry) fully reverted.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=I003`, `{NEGATIVE_CASES}=the six cases themselves`, `{MOCKING_STRATEGY}=real stack; DB manipulation via psql where noted`.

**Manual Testing:** Execute all six; screenshot/record outcomes in the report.

**Success Criteria:** [ ] Six/six pass; Shared Integration phase checklist green.

**Next Step:** SHARED FINAL PHASE, STEP F001.

---

# SHARED FINAL PHASE (STEPS F001–F003) — ALL THREE MEMBERS TOGETHER

**Purpose:** Seed data for demos, the README, and the final quality sweep.

**Prerequisites:** Shared Integration phase complete.

**Deliverables:** Complete `data.sql`, `README.md`, and a codebase that passes `mvn compile`, `npm run build`, and a clean `uvicorn` start after a full-code review sweep.

**Dependencies:** Everything.

**Phase completion checklist:**
- [ ] Fresh database + seed gives an immediately demo-able app (admin login works)
- [ ] README lets a stranger run all three services from scratch
- [ ] Final sweep report shows zero unused imports/broken refs/TODOs

---

### STEP F001 — Seed Data Script

**Objective:** One idempotent `data.sql` producing a demo-ready database.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP F001 ONLY.
Replace/extend backend/src/main/resources/data.sql to seed exactly (COMMANDO.md
Section 20 reference list): 1 admin admin@carbontrace.dev / Admin123! (BCrypt hash
generated now,
is_email_verified=true), 1 verified auditor, 3 vendors, 12–16 emission factors
(keep the four GLOBAL/ANY rows from Section 15 plus country/fuel-specific rows like
India/ROAD/DIESEL and China/SEA/HEAVY_FUEL_OIL demonstrating the fallback chain;
source column labeled "demo values inspired by GLEC-style factors"), 3 sellers,
8 listings, 4 sample CALCULATED shipments (realistic coordinates so the map is
populated; consistent emissions math), 2 sample purchases (consistent decrements on
their listings and offset_tonnes on their shipments, plausible agent_reasoning,
SIM- references), 1 goal. All inserts idempotent (ON CONFLICT DO NOTHING keyed on
natural uniques). Verify by wiping and recreating the schema locally is NOT possible
on shared RDS — instead verify inserts are no-ops on second boot and totals are
self-consistent via testing/shared/step-F001-db-checks.sql.
```

**Expected Output:** Modified: `data.sql`. DB: complete demo dataset.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP F001`, `{STEP_SCOPE}=seed data`, extra:
```
13. Seed internal consistency: purchases' tonnes reflected in listings and shipments;
    emissions = weight × distance × factor for the 4 sample shipments.
14. Second boot inserts nothing new (idempotency proven by row counts).
15. Admin login with the seeded credentials works.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=F001`, `{NEGATIVE_CASES}=double-boot row-count equality`, `{MOCKING_STRATEGY}=psql count snapshots before/after restart`.

**Manual Testing:** Restart backend twice; login as `admin@carbontrace.dev`; dashboard, map, marketplace, purchases all populated.

**Success Criteria:** [ ] Demo-ready on first boot; idempotent.

**Next Step:** STEP F002.

---

### STEP F002 — README.md

**Objective:** Complete setup + run documentation with disclaimers.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and implement STEP F002 ONLY.
Write the repo-root README.md covering: project overview + resume highlights
(Section 1), architecture diagram (Section 5, ASCII is fine), the one-time AWS setup
(Section 17 verbatim steps: RDS, S3 + CORS JSON, IAM least-privilege policy), Gmail
app password steps, Gemini key steps, .env.example walkthrough, run commands for all
three processes (mvn spring-boot:run / npm run dev / uvicorn main:app --reload),
seeded demo credentials, and PROMINENT disclaimers: emission factors are demo values
inspired by GLEC-style factors (not certified), and the marketplace + payments are
entirely simulated with no real transactions. No deployment/Docker/CI content
(explicitly out of scope). Update .env.example if any variable drifted during the
build.
```

**Expected Output:** Created: `README.md`. Possibly updated: `.env.example`.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP F002`, `{STEP_SCOPE}=README`, extra:
```
13. Both disclaimers present and prominent; zero deployment content; every env var in
    .env.example is explained; commands copy-paste runnable.
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=F002`, `{NEGATIVE_CASES}=n/a`, `{MOCKING_STRATEGY}=a "stranger test": follow the README verbatim in a clean shell and note any gap`.

**Manual Testing:** Fresh terminal, follow README top-to-bottom; every command works or the README gets fixed.

**Success Criteria:** [ ] README is self-sufficient; disclaimers present.

**Next Step:** STEP F003.

---

### STEP F003 — Final Verification Sweep

**Objective:** Whole-codebase review across all three services; final builds green.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and execute STEP F003 ONLY.
Read the ENTIRE codebase across backend/, frontend/, ai-service/ and fix ONLY:
unused imports, broken references, missing error handling, inconsistent ApiResponse
formats, dead code, lingering TODO/placeholder text, secrets accidentally in tracked
files, and log statements leaking sensitive data. Confirm against COMMANDO.md
Section 24 non-negotiables (two services only, layered architecture, sync-only,
11 tables, tech constraints, AI usage boundaries, simulated-marketplace labeling).
Then run and paste the results of: mvn -f backend/pom.xml compile,
cd frontend && npm run build, and a clean uvicorn main:app startup.
Produce testing/shared/step-F003-final-report.md listing every fix and each Section 24
constraint with PASS.
```

**Expected Output:** Cleanup diffs; final report; three green builds.

**Verification Prompt:** Template 0.3, `{STEP_ID}=STEP F003`, `{STEP_SCOPE}=final sweep`, extra:
```
13. Every Section 24 constraint explicitly checked PASS with evidence.
14. `grep -ri "TODO" --include="*.java" --include="*.jsx" --include="*.py"` → empty.
15. Exactly 11 tables in RDS (\dt count).
```

**Testing Prompt:** Template 0.4, `{STEP_ID}=F003`, `{NEGATIVE_CASES}=n/a`, `{MOCKING_STRATEGY}=n/a — run the STEP I002 smoke sequence one final time after cleanup to confirm nothing regressed`.

**Manual Testing:** Three build commands; one final smoke pass; `git log --oneline` shows one commit per step.

**Success Criteria:**
- [ ] Three services build/start clean
- [ ] Section 24 all PASS; 11 tables exactly
- [ ] Final smoke pass green

**Next Step:** DONE. The project is complete. Proceed to the FINAL PROMPT below to generate the deployment guide.

---

## 🚀 FINAL PROMPT — Generate the AWS Deployment Guide (run ONLY after everything above is complete)

**Precondition — do NOT run this early.** Every step of every track must be
finished, all three services must build and start clean, the Section 24
constraint check must pass, and the final smoke pass must be green. Deployment
content is out of scope for COMMANDO.md and for this build guide; this prompt
produces a **separate** file, exactly as COMMANDO.md's scope note anticipates
("A separate instruction file will be created later for the deployment phase").

Running this prompt earlier will pull deployment concerns into a codebase that
COMMANDO.md Section 24 requires to contain none.

**Prompt to Claude Code / Antigravity:**
```
Read COMMANDO.md and CARBONTRACE_BUILD_GUIDE.md in full, then create a NEW file
CARBONTRACE_DEPLOYMENT_GUIDE.md at the repo root. Create ONLY that file — do not
modify application code, and do not add Dockerfiles, CI pipelines, or IaC to the
project itself.

Target: a BASIC-to-MEDIUM complexity deployment on AWS, staying inside free-tier
/ minimal-cost options wherever possible. This is a portfolio project, not a
production system. Prefer the simplest thing that works and is demonstrable.

The guide must cover, in this order:

1. PRE-DEPLOYMENT DATABASE SWITCH (do this FIRST, before anything else):
   - Provision Amazon RDS PostgreSQL 16 per MANUAL SETUP A.2 of the build guide
     (if not already done).
   - Migrate from the local PostgreSQL 16 used throughout development: since
     Hibernate ddl-auto=update recreates the 11 Section 7 tables automatically,
     document BOTH paths — (a) let Hibernate create the empty schema on first
     boot, and (b) pg_dump/pg_restore if any seeded reference data
     (emission_factors, sellers, carbon_credit_listings) must be carried over.
   - Switch configuration ONLY: DB_URL / DB_USERNAME / DB_PASSWORD, with
     ?sslmode=require. application.yml must stay verbatim per Section 19.
   - State explicitly that NO entity, repository, query, or business logic
     changes — both environments are PostgreSQL 16.
   - Verification: app boots against RDS, all 11 tables present, a smoke pass of
     the auth + upload + calculate + purchase flows.

2. Deployment topology for all three services (Spring Boot :8080, FastAPI :8000,
   React static build), with a clear recommendation and the trade-offs stated:
   simplest viable option first, and what each costs.

3. Networking and security, corrected for the deployed model:
   - RDS becomes PRIVATE (Public access = No); its security group references the
     application's security group instead of any IP address. Explain that this
     removes the rotating-public-IP problem that forced local-first development.
   - S3 stays private; presigned URLs only; update the bucket CORS AllowedOrigins
     from http://localhost:5173 to the deployed frontend origin.
   - Where each secret lives (never in the repo): DB credentials, JWT_SECRET,
     AWS keys, GMAIL_APP_PASSWORD, GEMINI_API_KEY.
   - IAM: keep the least-privilege S3 policy from COMMANDO.md Section 17.

4. Configuration changes required at deploy time, as an exhaustive table of every
   environment variable from COMMANDO.md Section 19: local dev value → deployed
   value → where it is set.

5. Frontend build and hosting: VITE_API_BASE_URL pointing at the deployed backend,
   and the CORS origin change on the Spring Boot side (COMMANDO.md Section 10).

6. Post-deployment verification checklist: every flow in COMMANDO.md Section 3
   exercised end to end against the deployed stack.

7. Cost control and teardown: what each resource costs, how to stop/delete
   everything, and an explicit warning that an idle RDS instance keeps consuming
   credits.

8. Rollback: how to point back at local PostgreSQL if the deployment fails.

Rules: no placeholders, no TODOs. Every command complete and runnable. Where the
AWS console UI is involved, give exact section and field labels. State clearly
which steps cost money and which are free-tier eligible.
```

**Success Criteria:**
- [ ] `CARBONTRACE_DEPLOYMENT_GUIDE.md` created; no deployment artifacts added to the application itself
- [ ] Local PostgreSQL → RDS switch documented as configuration-only, with verification
- [ ] Every Section 19 environment variable mapped local → deployed
- [ ] Teardown and cost warnings present

---

## APPENDIX A — Manual Setup Index

| Setup | Owner | When | Time | Blocks |
|---|---|---|---|---|
| A.1 — Local PostgreSQL 16 | Track A | before STEP A003 | ~10 min | all Track A DB work |
| A.2 — RDS PostgreSQL 16 | Track A | DEFERRED — before deployment (FINAL PROMPT) | ~20 min | nothing; not a build blocker |
| B — Gmail app password | Track A | before STEP A010 (deferrable to before STEP I002 with console fallback) | ~5 min | real OTP mail |
| C — S3 bucket + CORS + IAM | Track A | before STEP A016 | ~15 min | real uploads (Track B needs it only at B-SWAP-1) |
| D — Gemini API key | Track C | before STEP C001 (needed by C004/C005; the STEP C003 calculator works without it) | ~5 min | extraction + agent |

## APPENDIX B — Service Run Cheat Sheet

### Day One (per track — which mocks + which real services each member runs locally)

```
# Track A: real backend + mocks/mock-ai
#   (needs DB_*, AWS_*, MAIL_*, JWT env vars — COMMANDO.md Section 19 Track A notes)
cd mocks/mock-ai && <run per its README>                # :8000 (until A-SWAP-1..3)
cd backend && mvn spring-boot:run                        # :8080

# Track B: mocks/mock-backend only — no real services at all
cd mocks/mock-backend && <run per its README>            # :8080 (until B-SWAP-1)
cd frontend && npm run dev                               # http://localhost:5173

# Track C: real AI service + its two mocks
#   (needs ai-service/.env with GEMINI_API_KEY — Section 19 Track C notes)
cd mocks/mock-marketplace && <run per its README>        # :8080 locally (until C-SWAP-1)
cd mocks/sample-pdfs && <run static server per README>   # :9000 (fixtures; real path
                                                         #  proven at A-SWAP-1)
cd ai-service && . .venv/bin/activate && uvicorn main:app --reload   # :8000
```

Each member needs only their own track's env vars on day one (COMMANDO.md Section 19 per-track notes). No URL value ever changes at swap time — only which process listens on the port.

### Post-Integration (I/F phases — mocks deleted, everything real)

```
# Backend (needs DB_*, AWS_*, MAIL_*, JWT env vars)
cd backend && mvn spring-boot:run                        # :8080

# Frontend
cd frontend && npm run dev                               # http://localhost:5173

# AI service (needs ai-service/.env)
cd ai-service && . .venv/bin/activate && uvicorn main:app --reload   # :8000
```

## APPENDIX C — Golden Rules Recap

1. One step per prompt. Session-init prompt on every new session.
2. Implement → verify → test → manual-test → commit. Never proceed on red.
3. Three parallel tracks (COMMANDO.md Section 25): each developer starts at STEP A001 / B001 / C001 on day one; cross-track dependencies come from `mocks/` until the labeled swap steps (A-SWAP-1..3, B-SWAP-1, C-SWAP-1); only the I/F phases are joint.
4. The Commando File wins every disagreement. This guide only sequences it.
5. Nothing simulated is ever presented as real: SIMULATED labels are non-negotiable.
6. Section 8 is law — mocks copy it verbatim, code implements it verbatim, disagreements are fixed in COMMANDO.md first.
7. Never modify another track's directory.
8. A mock outliving its swap step is a defect.

**END OF BUILD GUIDE**
