package com.test.currencyrateservice.repository;

import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataR2dbcTest
class RateRepositoryTest {

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgres:16-alpine")
                  .withDatabaseName("testdb")
                  .withUsername("test")
                  .withPassword("test");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
    r.add("spring.r2dbc.username", postgres::getUsername);
    r.add("spring.r2dbc.password", postgres::getPassword);
    r.add("spring.flyway.enabled", () -> true);
    r.add("spring.flyway.url", postgres::getJdbcUrl);
    r.add("spring.flyway.user", postgres::getUsername);
    r.add("spring.flyway.password", postgres::getPassword);
  }

  @Autowired
  RateRepository repo;

  @Test
  void distinctOnReturnsLatestPerCurrency() {
    OffsetDateTime t1 = OffsetDateTime.now().minusMinutes(5);
    OffsetDateTime t2 = OffsetDateTime.now();

    RateEntity usd1 = RateEntity.builder().type(RateType.FIAT).currency("USD").rate(new BigDecimal("10")).fetchedAt(t1).build();
    RateEntity usd2 = RateEntity.builder().type(RateType.FIAT).currency("USD").rate(new BigDecimal("12")).fetchedAt(t2).build();
    RateEntity eur = RateEntity.builder().type(RateType.FIAT).currency("EUR").rate(new BigDecimal("11")).fetchedAt(t2).build();

    StepVerifier.create(
                    repo.saveAll(Flux.just(usd1, usd2, eur))
                            .thenMany(repo.findLatestPerCurrencyByType(RateType.FIAT))
                            .collectList()
            )
            .assertNext(list -> {
              assertThat(list).hasSize(2);
              assertThat(list).extracting(RateEntity::getCurrency).containsExactlyInAnyOrder("USD", "EUR");
              RateEntity usdLatest = list.stream().filter(e -> e.getCurrency().equals("USD")).findFirst().orElseThrow();
              assertThat(usdLatest.getRate()).isEqualByComparingTo("12");
            })
            .verifyComplete();
  }
}
