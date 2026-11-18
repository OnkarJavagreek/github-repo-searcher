# FreightFox Backend Assignment - GitHub Repository Searcher

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Swagger UI](https://img.shields.io/badge/Swagger-OpenAPI%203.0-blue.svg)](http://localhost:8080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

A production-grade RESTful microservice built with **Java 17** and **Spring Boot 3** that integrates with the GitHub REST API to search repositories, automatically **upserts** (updates existing or inserts new) repository metadata in a relational database, and exposes an API with dynamic filtering and sorting capabilities.

---

## 🌟 Key Features

1. **GitHub REST API Search Integration:**
   - Queries GitHub's `/search/repositories` API with keyword search, language filtering, and sorting.
   - Built using Spring 6's modern, fluent **`RestClient`**.
   - Optional GitHub Personal Access Token (PAT) support via environment variables (elevating limits from 60 req/hr to 5,000 req/hr).

2. **Idempotent Upsert Persistence:**
   - Preserves GitHub's unique repository `id` as primary key.
   - Automatically detects previously stored repositories and updates all metadata (`stars`, `forks`, `lastUpdated`, etc.) without creating duplicate rows.

3. **Dynamic Multi-Criteria Filtering & Sorting:**
   - Implemented using **Spring Data JPA Specifications (`JpaSpecificationExecutor`)** for composable, type-safe queries.
   - Filter by **Programming Language** (case-insensitive) and **Minimum Star Count** (`stars >= minStars`).
   - Dynamic sorting by `stars`, `forks`, or `updated` (defaults to `stars` descending).

4. **Production-Ready Exception Handling:**
   - Centralized `@RestControllerAdvice` mapping errors to standard RFC-compliant error payloads.
   - Gracefully handles GitHub API rate limiting (`429 Too Many Requests`), validation errors (`400 Bad Request`), and upstream gateway errors (`502 Bad Gateway`).

5. **Self-Documenting & Postman Ready:**
   - Interactive Swagger/OpenAPI 3.0 documentation at `/swagger-ui.html`.
   - Bundled Postman collection (`freightfox-github-searcher.postman_collection.json`) for quick one-click testing.

---

## 🏗️ Architecture & Design Patterns

The codebase follows **Clean Layered Architecture** with strict separation of concerns:

```
src/main/java/com/freightfox/githubsearcher/
├── client/                     # External GitHub API Client abstraction
│   ├── GithubApiClient.java
│   └── impl/GithubApiClientImpl.java
├── config/                     # OpenAPI/Swagger, RestClient & timeouts
├── controller/                 # REST Controllers (thin presentation layer)
│   └── GithubRepositoryController.java
├── dto/                        # Request/Response/GitHub API DTOs
├── entity/                     # JPA Entities (StoredRepository with indices)
├── exception/                  # Centralized @RestControllerAdvice & Custom Exceptions
├── repository/                 # Spring Data JPA Repository & Specifications
│   └── specification/RepositorySpecification.java
└── service/                    # Business Logic Layer & Upsert implementation
    └── impl/GithubSearchServiceImpl.java
```

### Design Patterns Used:
* **Repository Pattern:** Encapsulates data access logic using Spring Data JPA.
* **Specification Pattern:** Encapsulates reusable business search criteria (`RepositorySpecification`) dynamically evaluated at runtime.
* **DTO Pattern:** Decouples internal database representations from external API contracts and upstream GitHub models.
* **Strategy / Upsert Pattern:** Guarantees idempotency during high-frequency synchronization.

---

## 🔌 API Endpoints & Specifications

### 1. Search and Store Repositories
* **URL:** `POST /api/github/search`
* **Content-Type:** `application/json`

**Request Body:**
```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

**Response (200 OK):**
```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

---

### 2. Retrieve Stored Repositories
* **URL:** `GET /api/github/repositories`
* **Query Parameters (All Optional):**
  - `language` *(string)*: Filter by programming language (e.g. `Java`, `Python`).
  - `minStars` *(integer)*: Filter repositories having at least this many stars.
  - `sort` *(string)*: Sort field: `stars`, `forks`, or `updated` (default: `stars`).

**Example Request:**
```
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

**Response (200 OK):**
```json
{
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

---

## 🧪 Testing & TDD

Unit tests and controller slicing tests are written using **JUnit 5**, **Mockito**, and Spring's **MockMvc**.

To execute the test suite:
```bash
mvn clean test
```

### Test Coverage Highlights:
- **`GithubSearchServiceTest`:**
  - Verifies insertion of brand new repositories.
  - Verifies **upsert** behavior (updating existing records instead of duplicating).
  - Verifies dynamic filtering and default sorting.
  - Verifies validation and exception handling for invalid sort parameters.
- **`GithubRepositoryControllerTest`:**
  - MockMvc testing of `POST /api/github/search` with valid payload.
  - MockMvc testing for request validation (e.g. blank query -> `VAL_400`).
  - Rate limiting mapping (`429 Too Many Requests`).
  - MockMvc testing of `GET /api/github/repositories` with filter parameters.

---

## 🚀 How to Run Locally

### Prerequisites
- **JDK 17** or higher
- **Maven 3.8+**

### Option A: Quick Run with In-Memory H2 Database
```bash
git clone <your-repo-url>
cd github-repo-searcher
mvn spring-boot:run
```
The application starts on `http://localhost:8080`.
- **Swagger Documentation:** `http://localhost:8080/swagger-ui.html`
- **H2 Web Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:githubdb`, user: `sa`, password: blank)

### Option B: Run with PostgreSQL (Docker Compose)
```bash
docker run --name postgres-dev -e POSTGRES_DB=githubdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15

export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/githubdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

mvn spring-boot:run
```

---

## ☁️ Deployment Instructions (Railway / Render)

The project includes a multi-stage `Dockerfile`.

1. **Deploy on Railway / Render:**
   - Link the public GitHub repository.
   - Add a managed **PostgreSQL** database instance.
   - Configure the following Environment Variables:
     - `SPRING_PROFILES_ACTIVE`: `prod`
     - `SPRING_DATASOURCE_URL`: `${{Postgres.DATABASE_URL}}` (or standard jdbc connection string)
     - `SPRING_DATASOURCE_USERNAME`: `<db-user>`
     - `SPRING_DATASOURCE_PASSWORD`: `<db-password>`
     - `GITHUB_TOKEN`: *(Optional, for higher rate limits)*
2. Access the live Swagger documentation at `https://<your-app-domain>/swagger-ui.html`.

---

## 💡 Assumptions Made

1. **GitHub Unauthenticated Rate Limits:** Without a `GITHUB_TOKEN`, GitHub permits 60 searches per hour per IP. An optional token configuration is provided to ensure smooth evaluation without rate limit throttling.
2. **Page Size Limit:** Search requests fetch the top 30 repositories per search request from GitHub by default to keep latency low.
3. **Upsert Semantics:** GitHub repository `id` is guaranteed globally unique by GitHub. When a repository already stored in the DB is fetched again, its fields are refreshed to reflect the most current stars, forks, and updated timestamp.
4. **Sort Ordering:** All sorts (`stars`, `forks`, `updated`) are applied in descending order (highest to lowest / newest to oldest).

---

## 👨‍💻 Author
* **Onkar Kandekar**
* Email: [onkarkandekar05@gmail.com](mailto:onkarkandekar05@gmail.com)
* LinkedIn: [linkedin.com/in/Onkar-Kandekar](https://linkedin.com/in/Onkar-Kandekar)
* GitHub: [github.com/OnkarJavagreek](https://github.com/OnkarJavagreek)
