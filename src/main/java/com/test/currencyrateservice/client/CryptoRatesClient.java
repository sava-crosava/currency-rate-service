package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CryptoRatesClient {

  private final WebClient webClient;

  public CryptoRatesClient(@Qualifier("cryptoWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  public Mono<List<CryptoRateDto>> getRates() {
    return webClient
            .get()
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(CryptoRateDto.class)
            .collectList()
            .doOnSubscribe(s -> log.info("Fetching crypto rates"))
            .doOnSuccess(list -> log.info("Fetched crypto rates: {}", list.size()))
            .doOnError(e -> log.error("Crypto rates fetch failed", e));
  }
}
