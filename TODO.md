# Bloated Shelf — Implementation Plan

This document outlines the step-by-step implementation tasks for the Bloated Shelf project. Each task is sized to be manageable, testable, and comes with specific acceptance criteria that must be met before proceeding to the next.

## Phase 1: Project Skeleton & Database Foundation

### Task 1: Project Initialization & Maven Setup
- [ ] Initialize the basic Spring Boot project structure (Java 21).
- [ ] Update `pom.xml` with required dependencies: Spring Boot Web, Data JPA, Security, Flyway, PostgreSQL, Testcontainers, and `net.datafaker:datafaker`.
- [ ] Ensure correct version properties (Java 21).
- **Acceptance Criteria:** `mvn clean compile` succeeds, and a basic `BloatedShelfApplication` main class exists and boots without failure.

### Task 2: Core Configuration & Testcontainers Setup
- [ ] Create `src/main/resources/application.yml` with JPA/Hibernate configs (`ddl-auto: validate`, `show-sql: true`, `generate_statistics: true`), Flyway enabled, and `app.seed.*` properties.
- [ ] Create `application-dev.yml` (or Dev configuration class) setting up the `@ServiceConnection` bean for `PostgreSQLContainer` under the `dev` profile.
- **Acceptance Criteria:** Running the app with the `dev` profile successfully spins up the PostgreSQL testcontainer, connects to it, and shuts down cleanly.

### Task 3: Database Migrations (Flyway)
- [ ] Create `src/main/resources/db/migration/V1__create_schema.sql` defining all tables (`author`, `genre`, `book`, `book_genre`, `library_member`, `review`, `loan_record`) and constraints.
- [ ] Create `src/main/resources/db/migration/V2__seed_genres.sql` to insert 15 static genre rows.
- **Acceptance Criteria:** Application starts, Flyway connects to the Testcontainer DB, and successfully executes both migrations without syntax errors.

## Phase 2: Domain & Persistence

### Task 4: JPA Domain Entities
- [ ] Implement `Author`, `Genre`, `Book`, `LibraryMember`, `Review`, and `LoanRecord` entity classes.
- [ ] Map exact columns to fields (e.g., `BIGINT`, `VARCHAR(100)`).
- [ ] Establish relationships with explicit `fetch = FetchType.LAZY` and mappedBy definitions where necessary.
- **Acceptance Criteria:** Application starts cleanly. Hibernate validates the DDL (`ddl-auto: validate`) against the Flyway schema without mismatch errors.

### Task 5: Repository Interfaces
- [ ] Create `AuthorRepository`, `BookRepository`, `LibraryMemberRepository`, `ReviewRepository`, and `LoanRecordRepository`.
- [ ] Add the specific "naïve" methods (e.g., `findAllByOrderByLastNameAsc()`) required for N+1 endpoints.
- [ ] Add the specific `@Query` optimized methods with `JOIN FETCH` as outlined in the spec (for future reference/Admin).
- **Acceptance Criteria:** Application context successfully loads the repositories; Spring Data JPA query validation passes on boot.

## Phase 3: Data Seeding

### Task 6: Dynamic Data Seeder
- [ ] Create `DataSeeder` implementing `ApplicationRunner`, restricted to run only when the `seed` profile is active.
- [ ] Inject `app.seed.*` properties for generation counts.
- [ ] Use `net.datafaker.Faker` (with a fixed seed) to idempotently generate Authors, Books, Members, Reviews, and Loans.
- **Acceptance Criteria:** Starting the app with `spring.profiles.active=dev,seed` populates the DB. Logs verify records are inserted, and repeated startups do not duplicate authors/books (idempotency check).

## Phase 4: Core Logic & Anti-Patterns (N+1)

### Task 7: DTO Layer (Java Records)
- [ ] Implement all necessary response Records: `AuthorSummaryDto`, `BookSummaryDto`, `ReviewDto`, `BookWithReviewsDto`, `LoanDto`, `MemberWithLoansDto`, `StatsDto`, etc.
- [ ] Create static `from(Entity)` factories in the records to isolate mapping logic.
- **Acceptance Criteria:** All DTO records defined cleanly without compilation errors, strictly avoiding JPA entity exposure.

### Task 8: Service Layer (Intentionally Flawed)
- [ ] Create `AuthorService`, `BookService`, `MemberService`, and `AdminService`.
- [ ] Make them `@Transactional(readOnly = true)`.
- [ ] Implement the methods corresponding to REST API endpoints.
- [ ] *Crucial:* Add explicit comments documenting the N+1 queries. Trigger the lazy fetches inside the stream/maps.
- **Acceptance Criteria:** Services compile, inject correctly, and return the DTOs built from the naïve repository queries.

## Phase 5: Security & Web Delivery

### Task 9: Spring Security Configuration
- [ ] Implement `SecurityConfig` with HTTP Basic Authentication.
- [ ] Define the `UserDetailsService` bean with static users (`admin`, `librarian`, `member1`, `readonly`) and encoded passwords (`BCryptPasswordEncoder`).
- [ ] Add `@EnableMethodSecurity`.
- **Acceptance Criteria:** Accessing the application on any URL requires Basic Authentication. The `admin` user can log in.

### Task 10: REST Controllers
- [ ] Create `AuthorController`, `BookController`, `MemberController`, and `AdminController`.
- [ ] Wire them to the corresponding Service methods.
- [ ] Annotate methods/classes with `@PreAuthorize` based on role mappings (`VIEWER`, `MEMBER`, `LIBRARIAN`, `ADMIN`).
- **Acceptance Criteria:** Endpoints are mapped. Hitting the endpoints with Postman/cURL properly returns JSON data and generates massive N+1 `SELECT` statements in the console logs.

## Phase 6: Testing & Quality Assurance

### Task 11: Integration Testing
- [ ] Create `ApplicationIntegrationTest` annotated with `@SpringBootTest`, `@ActiveProfiles({"test", "seed"})`, and `@Testcontainers`.
- [ ] Setup the static `@Container` `PostgreSQLContainer`.
- [ ] Write context load tests and schema existence checks.
- [ ] Write assertions for DB row counts matching the seeded properties.
- [ ] Write HTTP smoke tests (MockMvc or TestRestTemplate) to verify correct Role enforcement (401, 403, and 200 codes).
- **Acceptance Criteria:** Running `mvn verify` executes the test suite successfully with 100% pass rate.
