package com.test.currencyrateservice.controller;

import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.service.CurrencyRatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CurrencyRatesController {

  private final CurrencyRatesService service;

  @GetMapping(path = "/currency-rates", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<CurrencyRatesResponse> getCurrencyRates() {
    log.info("GET /currency-rates called");
    return service.getCurrencyRates()
            .doOnSuccess(response -> log.info("GET /currency-rates success: {} fiat, {} crypto",
                    response.fiat().size(), response.crypto().size()))
            .doOnError(ex -> log.error("GET /currency-rates failed", ex));
  }
}
