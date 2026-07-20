# CarbonTrace Backend — Local Development

How to run the Spring Boot backend locally.

> **Never commit credentials.** This file documents variable **names** only.
> Real values belong in your shell, your IDE run configuration, or the
> git-ignored `application-local.yml` — never in a tracked file. `.gitignore`
> already covers `.env`, `application-local.yml`, and `target/`.

## Database environment policy (local-first)

Per COMMANDO.md Section 17 "Database Environment Policy", the database has two
environments and switching between them is a **configuration change only** — no
entity, repository, query, or business-logic change, because both are
PostgreSQL 16.

| Phase | Database |
|---|---|
| **Development + all testing** (now) | **Local PostgreSQL 16** at `localhost:5432/carbontrace` |
| **Pre-deployment onward** | **Amazon RDS PostgreSQL 16** |

S3 is **not** affected — it is real AWS from day one. Only the database is
local-first.

## Prerequisites

- **JDK 17** (COMMANDO.md Section 4 pins Java 17, not 21)
- **Maven 3.9+**
- **PostgreSQL 16** installed locally, with a `carbontrace` database
  (MANUAL SETUP A.1 in `CARBONTRACE_BUILD_GUIDE.md`):
  ```bash
  # Windows
  winget install --id PostgreSQL.PostgreSQL.16 --exact
  # then, with superuser password "postgres" on port 5432:
  psql -U postgres -c "CREATE DATABASE carbontrace;"
  ```

## Running the app

Because the local instance uses the COMMANDO.md Section 19 defaults exactly
(`localhost:5432/carbontrace`, user `postgres`, password `postgres`), the app
runs with **no environment variables and no profile**:

```bash
mvn -f backend/pom.xml spring-boot:run
```

The app listens on **http://localhost:8080** (`server.port: 8080`).

To run on a different port (useful when 8080 is occupied):

```bash
mvn -f backend/pom.xml spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### Overriding the database

Two mechanisms, in increasing precedence:

1. **`backend/src/main/resources/application-local.yml`** (git-ignored) — activated
   with `-Dspring-boot.run.profiles=local`, or permanently via
   `setx SPRING_PROFILES_ACTIVE local`.
2. **Environment variables** — these outrank config files:

| Variable | Purpose | Default (`application.yml`) |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/carbontrace` |
| `DB_USERNAME` | Username | `postgres` |
| `DB_PASSWORD` | Password | `postgres` |

PowerShell:
```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/carbontrace"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
```

## Switching to Amazon RDS (pre-deployment only)

Do this when you reach the deployment phase — MANUAL SETUP A.2 and the FINAL
PROMPT in `CARBONTRACE_BUILD_GUIDE.md`. Change configuration only:

```
DB_URL=jdbc:postgresql://<your-endpoint>:5432/carbontrace?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<your-rds-master-password>
```

**`?sslmode=require` is mandatory.** RDS PostgreSQL 15+ ships with
`rds.force_ssl=1` in the default parameter group and rejects unencrypted
connections. `application.yml` binds the URL straight from `${DB_URL}`, so the
query string passes through to the driver unchanged — no code change needed.

While RDS has `Public access = Yes`, its security group must allow your current
public IP. **Home and mobile IPs rotate**, silently breaking new connections
while already-established ones keep working. If connections start timing out,
re-set the inbound rule's Source to **My IP** (EC2 console → Security Groups).
This failure mode is the reason development is local-first.

## What a successful startup looks like

```
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http) with context path '/'
Started CarbonTraceApplication in X.XXX seconds
```

Expected, and **not** errors:

- **No `create table` statements.** There are no `@Entity` classes yet, so
  Hibernate creates nothing even though `ddl-auto: update` is set. Zero tables is
  the **correct** result until STEP A007.
- `Using generated security password: <uuid>` — Spring Security's default
  auto-configuration, because no `SecurityConfig` exists yet (STEP A006). This
  value is throwaway, regenerates on every restart, and is not a project
  credential.
- `The following 1 profile is active: "local"` — only if you activated the
  profile; absent otherwise, which is fine.

Verify from a SQL client:

```sql
SELECT version();        -- expect PostgreSQL 16.x
\dt                      -- expect: "Did not find any relations."
```

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Connection to localhost:5432 refused` | Local PostgreSQL service not running | Windows: `Get-Service postgresql*` then `Start-Service postgresql-x64-16` |
| `FATAL: database "carbontrace" does not exist` | Database never created | `psql -U postgres -c "CREATE DATABASE carbontrace;"` |
| `FATAL: password authentication failed for user "postgres"` | Local superuser password isn't `postgres` | Set `DB_PASSWORD` to your actual local password |
| `Web server failed to start. Port 8080 was already in use` | Another app holds 8080 | Find it: `Get-NetTCPConnection -LocalPort 8080`; stop it, or run on `--server.port=8081` |
| **RDS only:** hangs then `HikariPool-1 - Exception during pool initialization` / `SocketTimeoutException` | Your public IP changed | Re-set the security group inbound rule to **My IP** |
| **RDS only:** `FATAL: no pg_hba.conf entry ... no encryption` | `?sslmode=require` missing | Add it to `DB_URL` |

Note that a rotating-IP failure surfaces as the generic
`org.postgresql.util.PSQLException: The connection attempt failed.`, which looks
like a credentials problem but is not. Check TCP reachability first.

## Stopping

Press `Ctrl+C` in the terminal running `spring-boot:run`.
