<p align="center">
  <img src="images/bloated-shelf.png" alt="Bloated Shelf" width="480" />
</p>

<h1 align="center">Bloated Shelf</h1>

<p align="center">
  <em>A lovingly broken library API — here to teach you the N+1 query problem the hard way.</em>
</p>

<p align="center">
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-blue?logo=openjdk" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?logo=springboot" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white" />
  <img alt="Testcontainers" src="https://img.shields.io/badge/Testcontainers-yes-orange" />
  <img alt="License" src="https://img.shields.io/badge/license-MIT-lightgrey" />
</p>

---

## What is this?

**Bloated Shelf** is a self-contained Spring Boot application that **intentionally demonstrates the JPA/Hibernate N+1 query problem** — that notorious antipattern where loading a list of 200 books quietly fires 1,201 SQL queries behind your back.

Think of it as a teaching tool, a live demo prop, or a cautionary tale you can actually run on your laptop. The database is pre-seeded with authors, books, genres, reviews, and loan records — enough data to make the N+1 blowup painfully visible in the console logs. No profiler needed.

> "It's not a bug, it's a feature demonstration." — *Every developer who has accidentally done this in production.*

---

## Why does this exist?

Because **seeing is believing**. Reading about the N+1 problem is one thing. Watching Hibernate print 1,200 prepared statements to your terminal in response to a single `GET /api/books` call is... memorable.

This project is designed to:

- 🔥 **Show** the N+1 anti-pattern in action across multiple entity levels
- 💡 **Explain** exactly which queries fire and why, via comments in service methods
- 🔬 **Contrast** with admin endpoints that use efficient JOIN queries, so you can see the difference side-by-side
- 🔒 **Include** Spring Security with role-based access, because real apps have auth too

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.1 |
| ORM | Hibernate 7.2.x (via Spring Data JPA) |
| Database | PostgreSQL 16 |
| Migrations | Flyway 11.x |
| Dev/Test DB | Testcontainers (`postgres:16-alpine`) |
| Fake data | [DataFaker](https://github.com/datafaker-net/datafaker) 2.x |
| Security | Spring Security 7.x (HTTP Basic) |
| Testing | JUnit 5 + Spring Boot Test + MockMvc |

---

## Domain Model

A classic library system — four entity levels, all lazily loaded, all primed for N+1 chaos:

```
Author
  └── Book
        ├── Genre       (many-to-many)
        ├── Review      (→ LibraryMember)
        └── LoanRecord  (→ LibraryMember)
```

The database is seeded with:

| Entity | Default count |
|---|---|
| Authors | 30 |
| Books | 200 |
| Library members | 50 |
| Reviews | ~5 per book |
| Loan records | ~8 per member |

All generated deterministically with a fixed Faker seed — so your results are reproducible.

---

## Getting Started

You'll need **Java 21**, **Maven 3.9+**, and **Docker** (for Testcontainers). That's it — no external database to set up.

### Run in dev mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev,seed
```

Testcontainers spins up a PostgreSQL 16 container, Flyway migrates the schema, and `DataSeeder` fills it with fake-but-plausible library data. App starts at `http://localhost:8080`.

### Run the tests

```bash
mvn verify
```

The integration test suite brings up its own Testcontainer and validates schema, seed counts, and HTTP auth behavior. Docker must be running.

### Run against an external PostgreSQL

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bloatedshelf
export SPRING_DATASOURCE_USERNAME=demo
export SPRING_DATASOURCE_PASSWORD=demo
export SPRING_PROFILES_ACTIVE=seed
mvn spring-boot:run
```

---

## Authentication

The API uses **HTTP Basic**. Four static users are pre-configured:

| Username | Password | Role(s) |
|---|---|---|
| `admin` | `admin123` | ADMIN, LIBRARIAN, MEMBER, VIEWER |
| `librarian` | `lib123` | LIBRARIAN, MEMBER, VIEWER |
| `member1` | `member123` | MEMBER, VIEWER |
| `readonly` | `read123` | VIEWER |

---

## API Endpoints

All endpoints are **GET only** — this is a read-only demo.

### Authors `(VIEWER)`
| Endpoint | Description |
|---|---|
| `GET /api/authors` | All authors with their books — N+1 per author |
| `GET /api/authors/{id}` | Author detail with books, genres, and avg ratings — cascading N+1 |
| `GET /api/authors/{id}/books` | Books for one author including genre names |

### Books `(MEMBER)`
| Endpoint | Description |
|---|---|
| `GET /api/books` | All books with all reviews + reviewer names — serious N+1 territory |
| `GET /api/books/{id}` | Book detail: author, genres, reviews, members — the full cascade |
| `GET /api/books/{id}/reviews` | Reviews for one book (member loaded per review) |
| `GET /api/books/by-genre/{genreId}` | Books by genre with same overfetch as `/api/books` |

### Members `(LIBRARIAN)`
| Endpoint | Description |
|---|---|
| `GET /api/members` | All members with loan records (book + author per loan) |
| `GET /api/members/{id}` | Member detail: loans → book → author + genres |
| `GET /api/members/{id}/active-loans` | Unreturned loans only, still naïvely loaded |

### Admin `(ADMIN)`
| Endpoint | Description |
|---|---|
| `GET /api/admin/stats` | Row counts per table — five clean `COUNT` queries, no N+1 |
| `GET /api/admin/most-loaned` | Top 10 most-loaned books — **single JOIN query**, the hero of the story |

---

## Observing the N+1 Problem

With `show-sql: true` and `generate_statistics: true` active by default, hit the books endpoint and watch your console light up:

```bash
curl -u member1:member123 http://localhost:8080/api/books | jq length
```

You'll see the Hibernate session metrics at the end of the request:

```
Session Metrics {
    ...
    2210 nanoseconds spent executing 1201 JDBC prepared statements;
    ...
}
```

**1,201 queries. For a single HTTP GET.** 😬

Now contrast it with the efficient admin endpoint:

```bash
curl -u admin:admin123 http://localhost:8080/api/admin/most-loaned | jq .
```

One query. One. The service methods are annotated with comments explaining exactly what Hibernate fires and why, so you can follow along in the code.

---

## Project Structure

```
src/
  main/java/com/example/bloatedshelf/
    config/          # SecurityConfig, DataSeeder, DevContainerConfig
    domain/          # JPA entities (all LAZY, intentionally)
    repository/      # Naïve queries + JOIN FETCH alternatives sitting unused
    service/         # The N+1 crime scene — documented with //N+1 DEMO comments
    web/             # REST controllers with @PreAuthorize role guards
    dto/             # Java records — entities never leave the service layer
  main/resources/
    application.yml
    db/migration/    # V1__create_schema.sql, V2__seed_genres.sql
  test/java/com/example/bloatedshelf/
    ApplicationIntegrationTest.java
```

---

## Out of Scope (v1.0)

This is a deliberate showcase of the *problem*, not the solution. These are natural next steps if you want to extend it:

- ✅ Optimised endpoint variants using `@EntityGraph` or `JOIN FETCH` (the repositories already have the queries — just wire them up)
- Pagination to limit the N+1 blast radius
- DTO projections (Spring Data interface projections / JPQL constructor expressions)
- Actuator + Micrometer exposing Hibernate statistics as metrics
- OpenAPI / Swagger UI
- Write endpoints (POST/PUT/DELETE)

---

## Contributing

This is primarily a teaching tool, but PRs are welcome — especially if they add new N+1 scenarios, improve the seeder, or extend the test suite. Just keep the intentionally-broken service methods intentionally broken. 😄

---

<p align="center">Made with ☕, questionable JOIN strategies, and a deep appreciation for SQL logs.</p>
