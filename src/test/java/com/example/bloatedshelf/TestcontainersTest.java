package com.example.bloatedshelf;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestcontainersTest {
    @Test
    void testConnection() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
            postgres.start();
            System.out.println("Started! " + postgres.getJdbcUrl());
        }
    }
}
