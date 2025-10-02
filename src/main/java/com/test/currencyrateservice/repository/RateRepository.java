package com.test.currencyrateservice.repository;

import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RateRepository extends ReactiveCrudRepository<RateEntity, Long> {

  @Query("""
    SELECT DISTINCT ON (currency)
      id, rate_type, currency, rate, fetched_at
    FROM rates
    WHERE rate_type = :type
    ORDER BY currency, fetched_at DESC
  """)
  Flux<RateEntity> findLatestPerCurrencyByType(RateType type);
}
