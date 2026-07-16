# User Service

A sample Spring Boot REST API for managing users, demonstrating a GitHub → GitHub Actions pipeline that builds, scans, and publishes artifact/inventory data to **CloudBees Unify**.

Part of the **fitness-calculator** example app set, alongside [notification-service](https://bitbucket.org/cbci-integration/notification-service) (Bitbucket / CBCI).

## Pipeline

```
GitHub (this repo) --> GitHub Actions
  --> Build & Test (Maven)
  --> SonarQube Analysis --> Unify Security Center (SARIF, TODO: confirm publish path)
  --> Docker Build --> Push to JFrog Artifactory
  --> Register Artifact --> CloudBees Unify (Inventory) via cloudbees-io-gha/register-build-artifact
  --> [experimental, best-effort] Trivy + Grype container scans
```

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml) for the full workflow definition.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users` | Create a user |
| GET | `/users/{id}` | Get user by ID |
| GET | `/actuator/health` | Spring Actuator health |

## Quick Start

### Local Development

```bash
# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/user-service-1.0.0.jar

# Access at http://localhost:8080
```

### Docker

```bash
docker build -t user-service:latest .
docker run -p 8080:8080 user-service:latest
```

## CI/CD Setup

This workflow expects the following repo secrets to be configured before it goes fully green:

- `JFROG_URL`, `JFROG_USER`, `JFROG_TOKEN` — JFrog Artifactory instance and credentials
- `SONAR_HOST_URL`, `SONAR_TOKEN` — SonarQube instance and token
- A `user-service` project/component registered in SonarQube and in CloudBees Unify
- Confirmation of whether `cloudbees-io-gha/register-build-artifact` needs an explicit auth secret beyond the CloudBees GitHub App install on this org

The container-scan job (Trivy/Grype) runs with `continue-on-error: true` until its SARIF output and Unify registration are validated.
