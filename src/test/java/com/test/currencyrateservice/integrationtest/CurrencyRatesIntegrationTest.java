package com.test.currencyrateservice.integrationtest;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CurrencyRatesIntegrationTest {

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgres:16-alpine")
                  .withDatabaseName("testdb")
                  .withUsername("test")
                  .withPassword("test");

  @RegisterExtension
  static WireMockExtension wiremock = WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort())
          .build();

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
    r.add("spring.r2dbc.username", postgres::getUsername);
    r.add("spring.r2dbc.password", postgres::getPassword);
    r.add("spring.flyway.url", postgres::getJdbcUrl);
    r.add("spring.flyway.user", postgres::getUsername);
    r.add("spring.flyway.password", postgres::getPassword);
    r.add("rates.fiat.base-url", () -> "http://localhost:" + wiremock.getPort() + "/fiat-currency-rates");
    r.add("rates.fiat.api-key", () -> "secret-key");
    r.add("rates.fiat.api-header", () -> "X-API-KEY");
    r.add("rates.crypto.base-url", () -> "http://localhost:" + wiremock.getPort() + "/crypto-currency-rates");
    r.add("rates.http.timeout", () -> "3s");
  }

  @Autowired
  WebTestClient webTestClient;

  @Autowired
  ConnectionFactory connectionFactory;

  @BeforeEach
  void resetState() {
    wiremock.resetAll();
    DatabaseClient.create(connectionFactory)
            .sql("TRUNCATE TABLE rates")
            .fetch()
            .rowsUpdated()
            .block(Duration.ofSeconds(5));
  }

  @Test
  void happyPath_thenProviderFails_usesDbFallback() {
    wiremock.stubFor(get(urlEqualTo("/fiat-currency-rates"))
            .withHeader("X-API-KEY", equalTo("secret-key"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody("[{\"currency\":\"USD\",\"rate\":10.5},{\"currency\":\"EUR\",\"rate\":11.1}]")));
    wiremock.stubFor(get(urlEqualTo("/crypto-currency-rates"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody("[{\"name\":\"BTC\",\"value\":1.0},{\"name\":\"ETH\",\"value\":2.0}]")));

    webTestClient.get().uri("/currency-rates")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.fiat.length()").isEqualTo(2)
            .jsonPath("$.crypto.length()").isEqualTo(2);

    wiremock.stubFor(get(urlEqualTo("/fiat-currency-rates"))
            .withHeader("X-API-KEY", equalTo("secret-key"))
            .willReturn(aResponse().withStatus(500)));
    wiremock.stubFor(get(urlEqualTo("/crypto-currency-rates"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody("[{\"name\":\"BTC\",\"value\":3.0}]")));

    webTestClient.get().uri("/currency-rates")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.fiat.length()").isEqualTo(2)
            .jsonPath("$.crypto.length()").isEqualTo(1)
            .jsonPath("$.crypto[0].currency").isEqualTo("BTC");
  }

  @Test
  void bothFailAndDbEmpty_returnsEmptyArrays() {
    wiremock.stubFor(get(urlEqualTo("/fiat-currency-rates"))
            .withHeader("X-API-KEY", equalTo("secret-key"))
            .willReturn(aResponse().withStatus(500)));
    wiremock.stubFor(get(urlEqualTo("/crypto-currency-rates"))
            .willReturn(aResponse().withStatus(500)));

    webTestClient.get().uri("/currency-rates")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.fiat.length()").isEqualTo(0)
            .jsonPath("$.crypto.length()").isEqualTo(0);
  }
}
