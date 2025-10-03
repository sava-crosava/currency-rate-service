package com.test.currencyrateservice.repository;

import com.test.currencyrateservice.PostgresContainerBase;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@DataR2dbcTest
@Import(TestSchema.class)
class RateRepositoryIT extends PostgresContainerBase {

  @Autowired
  RateRepository repo;

  @Autowired
  DatabaseClient db;

  @BeforeEach
  void clean() {
    db.sql("TRUNCATE TABLE rates").fetch().rowsUpdated().block();
  }

  @Test
  void findLatestPerCurrencyByType_returns_latest_rows() {
    var now = OffsetDateTime.now();
    var earlier = now.minusHours(1);
    var items = Flux.just(
            new RateEntity(null, RateType.FIAT, "USD", new BigDecimal("1.0"), earlier),
            new RateEntity(null, RateType.FIAT, "USD", new BigDecimal("1.1"), now),
            new RateEntity(null, RateType.FIAT, "EUR", new BigDecimal("2.0"), earlier),
            new RateEntity(null, RateType.FIAT, "EUR", new BigDecimal("2.2"), now)
    );
    StepVerifier.create(repo.saveAll(items).thenMany(repo.findLatestPerCurrencyByType(RateType.FIAT)))
            .expectNextMatches(e -> e.getCurrency().equals("EUR") && e.getRate().compareTo(new BigDecimal("2.2"))==0)
            .expectNextMatches(e -> e.getCurrency().equals("USD") && e.getRate().compareTo(new BigDecimal("1.1"))==0)
            .verifyComplete();
  }
}
