package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.config.RatesClientProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FiatRatesClient {

  private final WebClient webClient;
  private final RatesClientProperties props;

  public Mono<List<FiatRateDto>> getRates() {
    return webClient
            .get()
            .uri(props.getFiat().getBaseUrl())
            .header("X-API-KEY", props.getFiat().getApiKey())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(FiatRateDto.class)
            .collectList();
  }
}
