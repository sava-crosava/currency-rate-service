package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.config.RatesClientProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CryptoRatesClient {

  private final WebClient webClient;
  private final RatesClientProperties props;

  public Mono<List<CryptoRateDto>> getRates() {
    return webClient
            .get()
            .uri(props.getCrypto().getBaseUrl())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(CryptoRateDto.class)
            .collectList();
  }
}
