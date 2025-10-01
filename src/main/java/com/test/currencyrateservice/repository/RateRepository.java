package com.test.currencyrateservice.repository;

import com.test.currencyrateservice.entity.RateEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RateRepository extends ReactiveCrudRepository<RateEntity, Long> {
  Flux<RateEntity> findByTypeOrderByFetchedAtDesc(String type);
}
