package com.example.bloatedshelf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "seed"})
@Testcontainers
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoadsAndSchemaExists() {
        // Assert context loads and test container is running
        assertThat(postgres.isRunning()).isTrue();

        // Assert all tables exist
        String checkTableSql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "author")).isTrue();
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "book")).isTrue();
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "genre")).isTrue();
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "book_genre")).isTrue();
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "library_member")).isTrue();
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "review")).isTrue();
        assertThat(jdbcTemplate.queryForObject(checkTableSql, Boolean.class, "loan_record")).isTrue();
    }

    @Test
    void seedDataCountsAreCorrect() {
        // Assert DB row counts matching the seeded properties (app.seed.* defaults)
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM author", Long.class)).isGreaterThanOrEqualTo(30L);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM book", Long.class)).isGreaterThanOrEqualTo(200L);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM library_member", Long.class)).isGreaterThanOrEqualTo(50L);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM review", Long.class)).isGreaterThan(0L);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loan_record", Long.class)).isGreaterThan(0L);
    }

    // --- HTTP Smoke Tests (Role Enforcement) ---

    @Test
    @WithMockUser(username = "readonly", roles = "VIEWER")
    void viewerCanAccessAuthors() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    @WithMockUser(username = "readonly", roles = "VIEWER")
    void viewerCannotAccessBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member1", roles = {"MEMBER", "VIEWER"})
    void memberCanAccessBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "member1", roles = {"MEMBER", "VIEWER"})
    void memberCannotAccessMembers() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN", "MEMBER", "VIEWER"})
    void librarianCanAccessMembers() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN", "MEMBER", "VIEWER"})
    void librarianCannotAccessAdminStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "LIBRARIAN", "MEMBER", "VIEWER"})
    void adminCanAccessStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authors").isNumber())
                .andExpect(jsonPath("$.books").isNumber());
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "LIBRARIAN", "MEMBER", "VIEWER"})
    void adminCanArchiveBook() throws Exception {
        mockMvc.perform(put("/api/admin/books/1/archive"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "member1", roles = {"MEMBER", "VIEWER"})
    void memberCannotArchiveBook() throws Exception {
        mockMvc.perform(put("/api/admin/books/1/archive"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "LIBRARIAN", "MEMBER", "VIEWER"})
    void archiveNonExistentBookReturns404() throws Exception {
        mockMvc.perform(put("/api/admin/books/99999/archive"))
                .andExpect(status().isNotFound());
    }
}
