# Bloated Shelf — Specification

> **Purpose:** A self-contained Spring Boot application for demonstrating the JPA/Hibernate N+1 query problem.
> Intended to be used as a teaching/demo tool, not a production codebase.

---

## 1. Project Identity

| Aspect | Value |
|---|---|
| Project name | Bloated Shelf |
| GitHub repo | `bloated-shelf` |
| Maven `artifactId` | `bloated-shelf` |
| Base Java package | `com.example.bloatedshelf` |
| Main class | `BloatedShelfApplication` |
| `spring.application.name` | `bloated-shelf` |

---

## 2. Goals

- Showcase the N+1 query problem through intentionally naïve JPA usage
- Be fully self-contained: only Java 21, Maven, and Docker required
- Provide pre-seeded data large enough to make N+1 painfully visible in logs
- Expose REST endpoints that trigger overfetching and nested lazy-loading
- Include Spring Security with role-based access to demonstrate `@PreAuthorize`
- Ship with at least one integration test covering context startup, seed counts, and HTTP auth

---

## 3. Technology Stack

| Component | Artifact | Version |
|---|---|---|
| Java | JDK | 21 (LTS) |
| Build | Apache Maven | 3.9+ |
| Framework | Spring Boot | 4.0.1 |
| Spring Framework | (transitive) | 7.0.x |
| ORM | Hibernate (transitive) | 7.2.x |
| Data access | Spring Data JPA | 2025.1.x |
| Security | Spring Security | 7.0.x |
| Database | PostgreSQL | 16 |
| Dev/test DB | Testcontainers postgresql | latest compatible |
| Migrations | Flyway (`spring-boot-starter-flyway`) | 11.x |
| Fake data | `net.datafaker:datafaker` | 2.x |
| Testing | JUnit 5 + Spring Boot Test | — |

---

## 4. Domain Model

**Library / Book Lending** — four entity levels with deliberate multi-level nesting.

### 3.1 Entity Hierarchy

```
Author
  └── Book  (many books per author)
        ├── Genre       (many-to-many, join table book_genre)
        ├── Review      (one-to-many; each review belongs to a LibraryMember)
        └── LoanRecord  (one-to-many; each loan belongs to a LibraryMember)

LibraryMember
  ├── Review      (one-to-many)
  └── LoanRecord  (one-to-many)
```

### 3.2 Tables & Columns

#### `author`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT GENERATED ALWAYS AS IDENTITY | PK |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| birth_year | SMALLINT | nullable |
| nationality | VARCHAR(100) | nullable |

JPA: `@OneToMany(fetch = LAZY) List<Book> books`

#### `genre`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT GENERATED ALWAYS AS IDENTITY | PK |
| name | VARCHAR(80) | NOT NULL, UNIQUE |

#### `book`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT GENERATED ALWAYS AS IDENTITY | PK |
| title | VARCHAR(255) | NOT NULL |
| author_id | BIGINT | FK → author.id, NOT NULL |
| isbn | VARCHAR(20) | UNIQUE, nullable |
| published_year | SMALLINT | nullable |
| available_copies | SMALLINT | NOT NULL DEFAULT 1 |

JPA:
- `@ManyToOne(fetch = LAZY) Author author`
- `@ManyToMany(fetch = LAZY) Set<Genre> genres` — join table `book_genre(book_id, genre_id)`
- `@OneToMany(fetch = LAZY, mappedBy = "book") List<Review> reviews`
- `@OneToMany(fetch = LAZY, mappedBy = "book") List<LoanRecord> loanRecords`

#### `library_member`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT GENERATED ALWAYS AS IDENTITY | PK |
| username | VARCHAR(60) | NOT NULL, UNIQUE |
| full_name | VARCHAR(200) | NOT NULL |
| email | VARCHAR(200) | NOT NULL, UNIQUE |
| member_since | DATE | NOT NULL |

JPA: `@OneToMany(fetch = LAZY, mappedBy = "member") List<LoanRecord> loanRecords`

#### `review`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT GENERATED ALWAYS AS IDENTITY | PK |
| book_id | BIGINT | FK → book.id, NOT NULL |
| member_id | BIGINT | FK → library_member.id, NOT NULL |
| rating | SMALLINT | NOT NULL, CHECK (rating BETWEEN 1 AND 5) |
| comment | TEXT | nullable |
| reviewed_at | TIMESTAMP | NOT NULL DEFAULT now() |

#### `loan_record`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT GENERATED ALWAYS AS IDENTITY | PK |
| book_id | BIGINT | FK → book.id, NOT NULL |
| member_id | BIGINT | FK → library_member.id, NOT NULL |
| loaned_at | DATE | NOT NULL |
| due_date | DATE | NOT NULL |
| returned_at | DATE | nullable — NULL means still on loan |

---

## 5. Database Setup

### 4.1 DDL — Flyway Migrations

Location: `src/main/resources/db/migration/`

| File | Purpose |
|---|---|
| `V1__create_schema.sql` | All tables, join tables, FK constraints, indexes |
| `V2__seed_genres.sql` | 15 static genre rows (reference data) |

> **Important:** Set `spring.jpa.hibernate.ddl-auto=validate`. Flyway is the sole DDL authority — never use `create` or `update`.

### 4.2 Dynamic Seed Data

A `@Component` implementing `ApplicationRunner` seeds data on startup when the `seed` Spring profile is active.

**Seed volumes** (configurable via `app.seed.*` properties):

| Entity | Default | Property |
|---|---|---|
| Author | 30 | `app.seed.authors` |
| Book | 200 | `app.seed.books` |
| LibraryMember | 50 | `app.seed.members` |
| Reviews per book (avg) | 5 | `app.seed.reviews-per-book` |
| Loans per member (avg) | 8 | `app.seed.loans-per-member` |

**Requirements:**
- Use `net.datafaker.Faker` with a fixed seed for deterministic, reproducible data
- Idempotent: skip seeding if `author` table already has rows
- Assign books randomly to authors; assign 1–3 random genres per book
- Generate loan dates in the past 2 years; ~30% of loans should have a non-null `returned_at`

---

## 6. Project Structure

```
src/
  main/
    java/com/example/nplusonedemo/
      NplusOneDemoApplication.java
      config/
        SecurityConfig.java
        DataSeeder.java
      domain/
        Author.java
        Book.java
        Genre.java
        LibraryMember.java
        Review.java
        LoanRecord.java
      repository/
        AuthorRepository.java
        BookRepository.java
        LibraryMemberRepository.java
        ReviewRepository.java
        LoanRecordRepository.java
      service/
        AuthorService.java
        BookService.java
        MemberService.java
      web/
        AuthorController.java
        BookController.java
        MemberController.java
        AdminController.java
      dto/
        AuthorSummaryDto.java
        AuthorDetailDto.java
        BookSummaryDto.java
        BookWithReviewsDto.java
        BookDetailDto.java
        ReviewDto.java
        LoanDto.java
        MemberWithLoansDto.java
        MemberDetailDto.java
        StatsDto.java
    resources/
      application.yml
      application-dev.yml          # Testcontainers datasource override
      db/migration/
        V1__create_schema.sql
        V2__seed_genres.sql
  test/
    java/com/example/nplusonedemo/
      integration/
        ApplicationIntegrationTest.java
```

---

## 7. Configuration

### 6.1 `application.yml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true   # prints JDBC statement count per session
  flyway:
    enabled: true

app:
  seed:
    authors: 30
    books: 200
    members: 50
    reviews-per-book: 5
    loans-per-member: 8
```

> `generate_statistics: true` is the key observability lever — Hibernate logs the total number of prepared JDBC statements at end of each session, making N+1 unmistakable without a profiler.

### 6.2 Testcontainers (`application-dev.yml` / test config)

Use the Spring Boot 3.1+ `@ServiceConnection` pattern — no manual JDBC URL wiring needed:

```java
@Bean
@ServiceConnection
PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>("postgres:16-alpine");
}
```

Place this bean in a `@TestConfiguration` class. For the `dev` profile it can live in `src/main` under `config/` (guarded by `@Profile("dev")`). For tests, use `@ImportTestcontainers` or a shared abstract base class.

**`pom.xml` dependencies to add:**
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>testcontainers-bom</artifactId>
      <version>${testcontainers.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<!-- For dev profile Testcontainers bean in src/main -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-testcontainers</artifactId>
  <scope>compile</scope>  <!-- not test scope if used in src/main -->
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

---

## 8. Security

### 7.1 Authentication

HTTP Basic with in-memory `UserDetailsService`. No JWT/OAuth2 needed for this demo.

### 7.2 Static Users

| Username | Password | Roles | Persona |
|---|---|---|---|
| `admin` | `admin123` | ADMIN, LIBRARIAN, MEMBER, VIEWER | Full access |
| `librarian` | `lib123` | LIBRARIAN, MEMBER, VIEWER | Library staff |
| `member1` | `member123` | MEMBER, VIEWER | Regular patron |
| `readonly` | `read123` | VIEWER | Read-only observer |

Encode passwords with `BCryptPasswordEncoder`. Store the encoded hash in `SecurityConfig`, not plaintext.

### 7.3 Endpoint Authorization

Enable with `@EnableMethodSecurity` on `SecurityConfig`. Use `@PreAuthorize` on controller methods.

| Endpoint group | Required role |
|---|---|
| `GET /api/authors/**` | `VIEWER` (all authenticated) |
| `GET /api/books/**` | `MEMBER` |
| `GET /api/members/**` | `LIBRARIAN` |
| `GET /api/loans/**` | `LIBRARIAN` |
| `GET /api/admin/**` | `ADMIN` |

- Unauthenticated → `401 Unauthorized`
- Wrong role → `403 Forbidden`

---

## 9. REST API Endpoints

All endpoints are **GET only** — the database is pre-seeded; no writes are exposed.

### 8.1 Author Endpoints

| Path | Role | Response shape | N+1 demonstrated |
|---|---|---|---|
| `GET /api/authors` | VIEWER | `List<AuthorWithBooksDto>` — each author includes their list of books (title, year) | For each author, a separate SELECT fires to load books |
| `GET /api/authors/{id}` | VIEWER | `AuthorDetailDto` — author + books, each book includes genres + average rating | Cascading N+1: genres loaded per book, reviews aggregated per book |
| `GET /api/authors/{id}/books` | VIEWER | `List<BookSummaryDto>` — books with embedded genre names | N+1 on the genres join table per book |

### 8.2 Book Endpoints

| Path | Role | Response shape | N+1 demonstrated |
|---|---|---|---|
| `GET /api/books` | MEMBER | `List<BookWithReviewsDto>` — every book includes all reviews; each review embeds reviewer's `full_name` | N+1: reviews per book, then member per review |
| `GET /api/books/{id}` | MEMBER | `BookDetailDto` — book + author + genres + reviews (with reviewer name) | Cascading N+1 across author, genres, reviews → member |
| `GET /api/books/{id}/reviews` | MEMBER | `List<ReviewDto>` — reviews with reviewer name | N+1: member loaded per review |
| `GET /api/books/by-genre/{genreId}` | MEMBER | `List<BookWithReviewsDto>` | Same overfetch as `/api/books`; genre join also lazy |

### 8.3 Member Endpoints

| Path | Role | Response shape | N+1 demonstrated |
|---|---|---|---|
| `GET /api/members` | LIBRARIAN | `List<MemberWithLoansDto>` — each member includes all loan records; each loan includes book title + author name | N+1: loans per member, then book + author per loan |
| `GET /api/members/{id}` | LIBRARIAN | `MemberDetailDto` — member + loans (with book title, author, genre list) | Cascading N+1: loans → book → author and book → genres |
| `GET /api/members/{id}/active-loans` | LIBRARIAN | `List<LoanDto>` — unreturned loans only, with book details | Filtered N+1: lazy load per active loan |

### 8.4 Admin Endpoints

| Path | Role | Response shape | Notes |
|---|---|---|---|
| `GET /api/admin/stats` | ADMIN | `StatsDto` — row counts per table | No N+1 — uses `@Query` with `COUNT` |
| `GET /api/admin/most-loaned` | ADMIN | Top 10 books by loan count (title, author, count) | Intentionally efficient — single JOIN query; use as contrast |

---

## 10. Repository Layer

All repositories extend `JpaRepository<T, Long>`.

Each repository must provide:
1. A **naïve method** used by the N+1 demo service paths (typically plain `findAll()` or a simple derived query)
2. At least one **`@Query`-annotated method** — some demonstrating filters, some demonstrating the corrected JOIN FETCH path (for future comparison)

### `BookRepository`

```java
// Naïve — used by N+1 demo paths; reviews and member loaded lazily later
List<Book> findAll();

// Corrected path (JOIN FETCH) — for future comparison/solution branch
@Query("""
    SELECT DISTINCT b FROM Book b
    JOIN FETCH b.author
    LEFT JOIN FETCH b.genres
    """)
List<Book> findAllWithAuthorAndGenres();

// Filtered by genre
@Query("SELECT b FROM Book b JOIN b.genres g WHERE g.id = :genreId")
List<Book> findByGenreId(@Param("genreId") Long genreId);

// Admin: top 10 most loaned — single efficient query
@Query("""
    SELECT b.title, a.lastName, COUNT(lr)
    FROM Book b
    JOIN b.author a
    JOIN b.loanRecords lr
    GROUP BY b.id, a.id
    ORDER BY COUNT(lr) DESC
    """)
List<Object[]> findTop10MostLoaned(Pageable pageable);
```

### `AuthorRepository`

```java
// Naïve — books resolved lazily per author
List<Author> findAllByOrderByLastNameAsc();

// With books prefetched
@Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
Optional<Author> findByIdWithBooks(@Param("id") Long id);
```

### `LoanRecordRepository`

```java
// Active loans for a member (naïve — book + author still lazy)
@Query("""
    SELECT lr FROM LoanRecord lr
    WHERE lr.member.id = :memberId
    AND lr.returnedAt IS NULL
    """)
List<LoanRecord> findActiveLoansByMemberId(@Param("memberId") Long memberId);

// Corrected path — all associations prefetched
@Query("""
    SELECT lr FROM LoanRecord lr
    JOIN FETCH lr.book b
    JOIN FETCH b.author
    JOIN FETCH lr.member
    WHERE lr.member.id = :memberId
    """)
List<LoanRecord> findLoansByMemberIdWithDetails(@Param("memberId") Long memberId);
```

### `ReviewRepository`

```java
@Query("SELECT r FROM Review r WHERE r.book.id = :bookId ORDER BY r.reviewedAt DESC")
List<Review> findByBookId(@Param("bookId") Long bookId);
```

---

## 11. Service Layer

Services are `@Service` + `@Transactional(readOnly = true)`.

The N+1 anti-pattern lives here: services call naïve repository methods and then access lazy associations inside the transaction, forcing Hibernate to issue additional SELECTs.

Every service method that demonstrates N+1 must have a comment block explaining what queries Hibernate will fire:

```java
// N+1 DEMO:
// 1 SELECT for all books
// + 1 SELECT per book to load reviews  (N queries)
// + 1 SELECT per review to load member (N*M queries)
// Total: 1 + N + N*M queries for a single HTTP request
public List<BookWithReviewsDto> getAllBooksWithReviews() {
    return bookRepository.findAll()
        .stream()
        .map(book -> {
            var reviews = book.getReviews();          // triggers SELECT per book
            return BookWithReviewsDto.from(book, reviews);  // triggers SELECT per review (member)
        })
        .toList();
}
```

---

## 12. DTO Design

All response bodies are **Java records**. No JPA entities are serialized directly — this prevents accidental lazy-loading outside a transaction and makes the response contract explicit.

```java
public record AuthorSummaryDto(Long id, String firstName, String lastName) {}

public record BookSummaryDto(Long id, String title, int publishedYear,
                              AuthorSummaryDto author) {}

public record ReviewDto(Long id, int rating, String comment,
                        String reviewerName, LocalDateTime reviewedAt) {}

public record BookWithReviewsDto(Long id, String title,
                                  AuthorSummaryDto author,
                                  List<String> genreNames,
                                  List<ReviewDto> reviews) {}

public record LoanDto(Long id, BookSummaryDto book,
                      LocalDate loanedAt, LocalDate dueDate,
                      LocalDate returnedAt) {}

public record MemberWithLoansDto(Long id, String username,
                                  String fullName, List<LoanDto> loans) {}

public record StatsDto(long authors, long books, long members,
                       long reviews, long loans) {}
```

Each record may include a static `from(Entity)` factory method to keep mapping logic colocated.

---

## 13. Integration Tests

### 12.1 `ApplicationIntegrationTest`

Single test class covering all of the following in one `@SpringBootTest` run:

#### Context & Schema
- [ ] Application context loads without errors
- [ ] Tables exist: `author`, `book`, `genre`, `book_genre`, `library_member`, `review`, `loan_record`

#### Seed Data Counts
- [ ] `author` count >= `app.seed.authors` (30)
- [ ] `book` count >= `app.seed.books` (200)
- [ ] `library_member` count >= `app.seed.members` (50)
- [ ] `review` count > 0
- [ ] `loan_record` count > 0

#### HTTP Smoke Tests (MockMvc / `TestRestTemplate`)

| Request | Credentials | Expected |
|---|---|---|
| `GET /api/authors` | `readonly` | `200`, JSON array, size > 0 |
| `GET /api/authors/{validId}` | `readonly` | `200`, body contains `books` array |
| `GET /api/books` | `member1` | `200`, JSON array |
| `GET /api/books` | `readonly` (VIEWER only) | `403` |
| `GET /api/books` | (no auth) | `401` |
| `GET /api/members` | `librarian` | `200`, JSON array |
| `GET /api/members` | `member1` (MEMBER, not LIBRARIAN) | `403` |
| `GET /api/admin/stats` | `admin` | `200`, body has count fields |
| `GET /api/admin/stats` | `librarian` (not ADMIN) | `403` |

### 12.2 Test Class Setup

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "seed"})
@Testcontainers
class ApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // tests...
}
```

- `static` container field + `@Container` keeps the container alive for the whole test class — no per-test restarts
- `seed` profile activates `DataSeeder` during context startup
- Use `@Autowired MockMvc` or `TestRestTemplate` for HTTP assertions

---

## 14. Running the Application

### Dev mode (no external DB)

```bash
# Docker must be running
mvn spring-boot:run -Dspring-boot.run.profiles=dev,seed
```

Testcontainers starts a PostgreSQL 16 container, Flyway migrates, DataSeeder populates. App available at `http://localhost:8080`.

### Tests

```bash
mvn verify   # Docker must be running
```

### External PostgreSQL

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nplusdemo
export SPRING_DATASOURCE_USERNAME=demo
export SPRING_DATASOURCE_PASSWORD=demo
export SPRING_PROFILES_ACTIVE=seed
mvn spring-boot:run
```

---

## 15. Observing the N+1 Problem

With `show-sql: true` + `generate_statistics: true`, a `GET /api/books` with 200 books will log roughly:

- `1` SELECT for the books table
- `200` SELECTs for reviews (one per book)
- `~1000` SELECTs for members (one per review)

Hibernate session metrics at end of request:
```
Session Metrics {
  ...
  2210 nanoseconds spent executing 1201 JDBC prepared statements;
  ...
}
```

Use `GET /api/admin/most-loaned` as the contrast: it issues a **single** JOIN query (`prepared statements: 1`).

---

## 16. Out of Scope (v1.0)

- Optimized endpoint variants using `@EntityGraph` or JOIN FETCH (natural v2 extension)
- Pagination (`@PageableDefault`) to limit N+1 blast radius
- DTO projections (Spring Data interface projections / JPQL constructor expressions)
- Actuator + Micrometer exposing Hibernate statistics as metrics
- OpenAPI / Swagger UI
- Write endpoints (POST/PUT/DELETE)
