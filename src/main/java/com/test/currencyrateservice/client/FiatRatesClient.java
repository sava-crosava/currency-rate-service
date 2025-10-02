package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.FiatRateDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class FiatRatesClient {

  private final WebClient webClient;

  public FiatRatesClient(@Qualifier("fiatWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  public Mono<List<FiatRateDto>> getRates() {
    return webClient
            .get()
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(FiatRateDto.class)
            .collectList()
            .doOnSubscribe(s -> log.info("Fetching fiat rates"))
            .doOnSuccess(list -> log.info("Fetched fiat rates: {}", list.size()))
            .doOnError(e -> log.error("Fiat rates fetch failed", e));
  }
}
