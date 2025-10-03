package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.CryptoRateDto;
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
public class CryptoRatesClient {

    @Qualifier("cryptoWebClient")
    private final WebClient webClient;

  public Flux<CryptoRateDto> getRates() {
    return webClient
            .get()
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(CryptoRateDto.class)
            .doOnSubscribe(s -> log.info("Fetching crypto rates"))
            .doOnNext(it -> log.debug("Crypto rate {}", it))
            .doOnError(e -> log.error("Crypto rates fetch failed", e));
  }
}
