[Español](README.md) | [English](README.en.md)

# Prices Service

REST service developed as a solution to a backend technical assessment using Java and Spring Boot.

The application allows querying the applicable price for a product from a specific brand at a given date and time.

When multiple prices are applicable for the same product and brand, the price with the **highest priority** is selected.

---

## Table of Contents

* [Technologies](#technologies)
* [Architecture](#architecture)
* [Project Structure](#project-structure)
* [Business Rule](#business-rule)
* [API](#api)
* [Error Handling](#error-handling)
* [Persistence](#persistence)
* [Tests](#tests)
* [Postman](#postman)
* [SOLID Principles](#solid-principles)
* [Technical Decisions](#technical-decisions)
* [Possible Production Improvements](#possible-production-improvements)
* [Running the Application](#running-the-application)

---

## Technologies

* Java 21
* Spring Boot 4.1.1
* Spring Web MVC
* Spring Data JPA
* H2 Database
* Maven
* JUnit 5
* AssertJ
* MockMvc
* Bean Validation

---

## Architecture

The project follows a **Hexagonal Architecture**, separating the domain from infrastructure details and input/output mechanisms.

```text
                         HTTP
                          │
                          ▼
                ┌──────────────────┐
                │ PriceController  │
                │   REST Adapter   │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │   PriceService   │
                │    Use Case      │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │      Price       │
                │     Domain       │
                └────────┬─────────┘
                         │
                         ▼
                ┌───────────────────┐
                │PriceRepositoryPort│
                │   Output Port     │
                └────────┬──────────┘
                         │
                         ▼
                ┌──────────────────┐
                │Repository Adapter│
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │ Spring Data JPA  │
                └────────┬─────────┘
                         │
                         ▼
                ┌──────────────────┐
                │   H2 Database    │
                └──────────────────┘
```

The diagram represents the **request flow**. The dependency direction is different: the domain defines `PriceRepositoryPort`, while the infrastructure adapter provides its implementation.

The domain does not depend on Spring, JPA, or H2.

### Model Separation

Independent models are used for each responsibility:

```text
PriceEntity
     │
     │ PriceEntityMapper
     ▼
   Price
     │
     │ PriceRestMapper
     ▼
PriceResponse
```

* `PriceEntity`: JPA-dependent persistence model.
* `Price`: infrastructure-independent domain model.
* `PriceResponse`: DTO defining the REST API contract.

---

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/prices_service/
│   │       ├── domain/
│   │       │   ├── exception/
│   │       │   ├── model/
│   │       │   ├── port/
│   │       │   └── service/
│   │       │
│   │       └── infrastructure/
│   │           └── adapter/
│   │               ├── in/
│   │               │   └── rest/
│   │               └── out/
│   │                   └── persistence/
│   │
│   └── resources/
│       ├── application.yml
│       └── data.sql
│
├── test/
│   └── java/
│       └── com/example/prices_service/
│           └── infrastructure/
│               └── adapter/
│                   ├── in/
│                   │   └── rest/
│                   └── out/
│                       └── persistence/
│
└── postman/
    ├── prices-service.postman_collection.json
    └── prices-service.postman_environment.json
```

---

## Business Rule

The query receives:

* `brandId`
* `productId`
* `applicationDate`

A price is applicable when:

```text
startDate <= applicationDate
AND
endDate >= applicationDate
```

If multiple prices are applicable, the one with the highest `priority` is selected.

The selection is delegated to the database using:

```sql
WHERE brand_id = ?
  AND product_id = ?
  AND start_date <= ?
  AND end_date >= ?
ORDER BY priority DESC
LIMIT 1
```

This avoids retrieving multiple records and performing filtering and sorting in application memory.

---

## API

### Get Applicable Price

```http
GET /api/v1/prices
```

### Parameters

| Parameter         | Type            | Required | Description                                  |
| ----------------- | --------------- | -------: | -------------------------------------------- |
| `applicationDate` | `LocalDateTime` |      Yes | Date and time for which the price is queried |
| `productId`       | `Long`          |      Yes | Product identifier                           |
| `brandId`         | `Long`          |      Yes | Brand identifier                             |

`applicationDate` uses the ISO-8601 format:

```text
yyyy-MM-dd'T'HH:mm:ss
```

Example:

```text
2020-06-14T16:00:00
```

The `T` is the standard ISO-8601 separator between the date and time components.

### Example

```http
GET http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

Using `curl`:

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

### 200 OK Response

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

---

## Error Handling

Exceptions are handled centrally through `GlobalExceptionHandler`.

| Situation                  | HTTP Status       |
| -------------------------- | ----------------- |
| Price found                | `200 OK`          |
| No applicable price        | `404 Not Found`   |
| Required parameter missing | `400 Bad Request` |
| Invalid date format        | `400 Bad Request` |
| Invalid parameter          | `400 Bad Request` |

### 404 Not Found

```json
{
  "timestamp": "2026-08-30T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No applicable price found for brandId=1, productId=35455, applicationDate=2019-01-01T00:00"
}
```

### 400 Bad Request

```json
{
  "timestamp": "2026-08-30T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input parameters: Required parameter 'applicationDate' is not present"
}
```

---

## Persistence

For the technical assessment, the application uses **H2 in-memory database**, avoiding external dependencies and allowing the project to run directly.

The schema is generated by Hibernate and the sample data is loaded from `data.sql`.

Main configuration:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:pricesdb;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

The H2 console is available at:

```text
http://localhost:8080/h2-console
```

Connection details:

| Field    | Value                  |
| -------- | ---------------------- |
| JDBC URL | `jdbc:h2:mem:pricesdb` |
| Username | `sa`                   |
| Password | *(empty)*              |

### Index

`PriceEntity` defines a composite index on:

```text
brand_id
product_id
start_date
end_date
```

These columns participate in the price lookup query, and the index is intended to improve query efficiency as the data volume increases.

---

## Tests

The application includes tests at different levels.

### Persistence Tests

`PriceRepositoryAdapterTest` uses `@DataJpaTest` and an in-memory H2 database to verify the persistence adapter behaviour.

The tests cover:

* Base price.
* Selection of the highest-priority price.
* No applicable price.
* Unknown brand.
* Different dates and time ranges.

### REST Integration Tests

`PriceControllerIntegrationTest` starts the complete Spring Boot context and uses `MockMvc` to test the REST endpoint.

The five main scenarios from the exercise are covered:

| Date             | Expected result |
| ---------------- | --------------- |
| 14/06/2020 10:00 | Price list 1    |
| 14/06/2020 16:00 | Price list 2    |
| 14/06/2020 21:00 | Price list 1    |
| 15/06/2020 10:00 | Price list 3    |
| 16/06/2020 21:00 | Price list 4    |

Additional scenarios:

* `404` when no applicable price exists.
* `400` when `applicationDate` is missing.
* `400` when `applicationDate` has an invalid format.

---

## Postman

The project includes a Postman collection and a local environment:

```text
postman/
├── prices-service.postman_collection.json
└── prices-service.postman_environment.json
```

The collection contains the five main scenarios from the exercise, together with validation and error scenarios.

The environment defines:

```text
baseUrl = http://localhost:8080
```

### Usage

1. Start the application.
2. Open Postman.
3. Import the collection.
4. Import the `Prices-service - Local` environment.
5. Select the environment.
6. Execute any of the available requests.

Using `{{baseUrl}}` allows the target server to be changed without modifying the collection requests.

---

## SOLID Principles

The design applies the SOLID principles in a practical way:

| Principle                 | Application                                                                                                                      |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| **Single Responsibility** | Each component has a specific responsibility: REST controller, use case, mappers, persistence, and error handling are separated. |
| **Open/Closed**           | New input or output adapters can be added without modifying the domain.                                                          |
| **Liskov Substitution**   | Any implementation of `PriceRepositoryPort` can replace the current adapter without modifying the use case.                      |
| **Interface Segregation** | `PriceRepositoryPort` exposes only the operation required by the use case.                                                       |
| **Dependency Inversion**  | The domain depends on `PriceRepositoryPort`, not on Spring Data JPA or the concrete persistence implementation.                  |

---

## Technical Decisions

### `BigDecimal`

`BigDecimal` is used for prices to avoid the precision issues associated with `float` and `double` when working with monetary values.

### `LocalDateTime`

`LocalDateTime` is used because the exercise requires both date and time but does not specify timezone information.

### Independent DTO

`PriceResponse` is separated from the domain model to keep the public API contract independent from the internal domain structure.

### JPA-independent Domain

`Price` contains no JPA annotations or dependencies.

Persistence is handled through `PriceEntity`, and the conversion between the persistence and domain models is centralized in `PriceEntityMapper`.

### Repository Port

The domain defines `PriceRepositoryPort`, while the infrastructure provides its implementation through `PriceRepositoryAdapter`.

This allows the persistence technology to be changed without modifying the use case.

### Centralized Exception Handling

`GlobalExceptionHandler` centralizes the conversion of exceptions into HTTP responses, keeping the controller focused on request handling.

---

## Possible Production Improvements

The implementation is focused on solving the technical assessment while keeping the solution simple and maintainable.

In a production environment, the following improvements could be considered:

* Persistent database instead of H2.
* Environment-specific configuration profiles (`dev`, `test`, `prod`).
* OpenAPI/Swagger documentation.
* Structured logging.
* Metrics and distributed tracing.
* Additional unit tests for the domain.
* Index review using execution plans against the production database.
* External configuration of credentials and infrastructure properties.
* Secret management through a dedicated system.
* Health checks and observability.

---

## Running the Application

### Run Tests

```bash
mvn clean test
```

### Start the Application

```bash
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```
