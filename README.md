# ConfigVault

ConfigVault is a RESTful API designed to version and audit application configuration changes across multiple environments (`DEV`, `STAGING`, `PROD`). Similar to DevSecOps metadata tools, it tracks every change, records who made it, and enables robust lifecycle management including environment promotion and version rollback.

## 🚀 Features

- **Configuration Versioning:** Line-by-line diff generation between any two versions of a configuration.
- **Audit Trail:** Comprehensive audit logging (CREATE, UPDATE, ROLLBACK, PROMOTE).
- **Environment Promotion:** Seamlessly promote configurations from `DEV` to `STAGING` and `PROD`.
- **Role-Based Access Control:** 
  - `DEVELOPER`: Can view configurations and modify `DEV` environments.
  - `ADMIN`: Full access, including rollback and environment promotion.
- **Stateless Authentication:** Secured with JWT tokens.
- **Interactive Documentation:** Fully integrated Swagger UI for exploring and testing endpoints.

## 🛠 Tech Stack

- **Java 21**
- **Spring Boot 3** (Spring Web, Spring Data JPA, Spring Security)
- **PostgreSQL** (Database)
- **Lombok** (Boilerplate reduction)
- **Java-Diff-Utils** (Diff generation)
- **Springdoc OpenAPI** (Swagger UI)
- **JUnit 5 & Mockito** (Unit testing)
- **Docker & Docker Compose** (Containerization)
- **GitHub Actions** (CI/CD)

## 📋 Prerequisites

To run this project locally, ensure you have the following installed:
- **Java 21**
- **Maven**
- **Docker** and **Docker Compose**

## ⚙️ Setup Instructions

### 1. Running with Docker Compose (Recommended)

You can spin up the entire application along with a PostgreSQL database using Docker Compose:

```bash
docker-compose up --build
```
The API will be available at `http://localhost:8080`.

### 2. Running Locally (Maven)

Start a PostgreSQL database manually (or use `docker-compose up -d db`). Then, run the application using the Maven wrapper:

```bash
# Set environment variables for the DB if they differ from the defaults:
# DB_HOST=localhost, DB_PORT=5432, DB_NAME=configvault, DB_USER=postgres, DB_PASS=postgres

./mvnw spring-boot:run
```

## 🔐 Security & Authentication

The API is secured using JWT. To access protected endpoints, you must first register and login.

### Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password", "role": "ADMIN"}'
```

### Login to obtain JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'
```
*Extract the `"token"` from the response and pass it as a `Bearer` token in the `Authorization` header for subsequent requests.*

## 🌐 API Endpoints

You can explore all endpoints visually via **Swagger UI**:  
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Configurations
- `GET /api/configs` - Get all configs.
- `GET /api/configs/{id}` - Get a specific config.
- `POST /api/configs` - Create a new config.
- `PUT /api/configs/{id}` - Update a config (creates a new version).
- `GET /api/configs/{id}/history` - View version history.
- `GET /api/configs/{id}/diff?v1={v1}&v2={v2}` - Get a unified diff between two versions.
- `POST /api/configs/{id}/rollback/{version}` - Rollback to a specific version (Admin only).
- `POST /api/configs/{id}/promote?to={STAGING|PROD}` - Promote config to another environment (Admin only).

### Audit Logs
- `GET /api/audit` - View all audit logs across the application.

## 🏗 Architecture Overview

1. **Entity Mappings:** 
   - `Config` (1:N) `ConfigVersion`: The core configuration metadata holds multiple immutable versions representing the state of the content over time.
   - `AuditLog`: An independent entity that tracks actions performed across the system (tied loosely via `configId`).
   - `User`: Manages authentication credentials and RBAC roles.
2. **Service Layer:** 
   - Operations on a `Config` immediately trigger the creation of a new `ConfigVersion` and a corresponding `AuditLog`.
   - Security constraints are enforced at the service level to ensure environment protection against unauthorized roles.
3. **Versioning Algorithm:** 
   - When a config is updated, a new version is appended with an incremented version number.
   - `java-diff-utils` is used to dynamically compute the unified diff between the plain text contents of any two distinct versions.
   - Rollback appends a new version (copying the content of the target rollback version) instead of mutating past history, preserving a true immutable audit trail.
