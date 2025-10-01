package com.test.currencyrateservice.controller;

import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.service.CurrencyRatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CurrencyRatesController {

  private final CurrencyRatesService service;

  @GetMapping(path = "/currency-rates", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<CurrencyRatesResponse> getCurrencyRates() {
    return service.getCurrencyRates();
  }
}
