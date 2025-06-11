# Testing Protocol

This document defines **exact, copy-pastable commands** and environment requirements for running *all* automated tests in the `realworld-mongodb` codebase.  
It covers:

1. Unit-test execution for each module  
2. API (integration) test execution via Newman  
3. Automation feasibility assessment  
4. Expected outputs / success criteria

---

## 1. Test Locations

| Test Type | Module / Path | Notes |
|-----------|---------------|-------|
| **Unit tests – API layer** | `server/api/src/test/java/**` | Spring‐Boot tests for controllers, config, etc. |
| **Unit tests – Core domain** | `module/core/src/test/java/**` | Pure JVM tests for entities & services. |
| **Unit tests – Persistence layer** | `module/persistence/src/test/java/**` | JPA repository & adapter tests. |
| **API test runner script** | `api-docs/run-api-tests.sh` | Executes Postman collection with Newman. |

---

## 2. Unit-Test Execution (Gradle)

Pre-condition: **Java 21** is available and the Gradle wrapper (`./gradlew`) is executable.

Clean build **and** run all unit tests for *every* module:

```bash
./gradlew clean test
```

Details:
* Gradle automatically discovers sub-projects and executes their test tasks.
* Output is written to `*/build/reports/tests/test/index.html` per module.

Success criteria:
* Command exits with `BUILD SUCCESSFUL`.
* No unit test fails.

---

## 3. API Test Execution (Newman/Postman)

### 3.1 Prerequisites

1. **Node ≥18** installed (needed for `npx`).
2. Application running locally on port **8080** with default H2 profile:

```bash
./gradlew :server:api:bootRun
```

3. Optional: make the runner executable once:

```bash
chmod +x api-docs/run-api-tests.sh
```

### 3.2 Command

In a **separate terminal**, run:

```bash
api-docs/run-api-tests.sh
```

Environment variables you may override:

| Variable | Default | Purpose |
|----------|---------|---------|
| `APIURL` | `http://localhost:8080/api` | Base URL under test |
| `USERNAME` | `u$(date +%s)` | Random username |
| `EMAIL` | `$USERNAME@mail.com` | Matching email |
| `PASSWORD` | `password` | Account password |

Example (using defaults):

```bash
APIURL=http://localhost:8080/api ./api-docs/run-api-tests.sh
```

Success criteria:
* Newman exits with status `0`.
* Console shows **“× 0 failed”** for every collection request.

---

## 4. Automation Feasibility

| Suite | Manual Interaction Required? | Automatable? |
|-------|------------------------------|--------------|
| Unit tests (`./gradlew clean test`) | No | **Yes** |
| API tests (`api-docs/run-api-tests.sh`) | Only prerequisite is running server; can be scripted | **Yes** |

**Overall determination:** **Yes** – the entire test suite can run unattended, provided environment prerequisites (Java 21, Node, free port 8080) are satisfied and the server is started in the background.

---

## 5. Environment Setup Summary

1. **Java 21** in `PATH`
2. **Gradle Wrapper** (`./gradlew`) executable
3. **Node ≥18** in `PATH`
4. **Port 8080** free (or set `APIURL` accordingly)
5. No further manual steps required

For CI environments, launch the application in one job/step (e.g., `./gradlew :server:api:bootRun &`) and then execute the API script in another step once the server is accepting connections.

---

## 6. Expected Outputs

| Command | Expected Result |
|---------|-----------------|
| `./gradlew clean test` | `BUILD SUCCESSFUL` and **0 failed** tests across all modules |
| `./gradlew :server:api:bootRun` | Spring banner, line `Started RealWorldApplication` with `Tomcat started on port(s): 8080` |
| `api-docs/run-api-tests.sh` | Summary ending with `failures: 0` (or `× 0 failed`) and exit code 0 |

---

**End of testing protocol**
