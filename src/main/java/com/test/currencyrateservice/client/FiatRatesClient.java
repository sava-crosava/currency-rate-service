package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.FiatRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
@Slf4j
@RequiredArgsConstructor
public class FiatRatesClient {

    @Qualifier("fiatWebClient")
    private final WebClient webClient;

  public Flux<FiatRateDto> getRates() {
    return webClient
            .get()
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(FiatRateDto.class)
            .doOnSubscribe(s -> log.info("Fetching fiat rates"))
            .doOnNext(it -> log.debug("Fiat rate {}", it))
            .doOnError(e -> log.error("Fiat rates fetch failed", e));
  }
}
