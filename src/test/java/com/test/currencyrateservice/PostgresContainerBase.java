package com.test.currencyrateservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresContainerBase {
  protected static final PostgreSQLContainer<?> POSTGRES =
          new PostgreSQLContainer<>("postgres:16-alpine")
                  .withDatabaseName("testdb")
                  .withUsername("test")
                  .withPassword("test");

  static { POSTGRES.start(); }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.r2dbc.url",
            () -> "r2dbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort() + "/" + POSTGRES.getDatabaseName());
    r.add("spring.r2dbc.username", POSTGRES::getUsername);
    r.add("spring.r2dbc.password", POSTGRES::getPassword);
    r.add("spring.sql.init.mode", () -> "never");
  }
}
