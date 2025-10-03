package com.test.currencyrateservice.repository;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
class TestSchema {

  @Bean
  DatabaseClient databaseClient(ConnectionFactory cf) {
    var client = DatabaseClient.create(cf);
    client.sql("""
        CREATE TABLE IF NOT EXISTS rates(
          id SERIAL PRIMARY KEY,
          rate_type VARCHAR(16) NOT NULL,
          currency VARCHAR(16) NOT NULL,
          rate NUMERIC(38, 10) NOT NULL,
          fetched_at TIMESTAMPTZ NOT NULL
        )
        """).fetch().rowsUpdated().block();
    return client;
  }
}
