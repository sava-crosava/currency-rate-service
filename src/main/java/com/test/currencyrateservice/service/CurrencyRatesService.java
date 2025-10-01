package com.test.currencyrateservice.service;

import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import reactor.core.publisher.Mono;

public interface CurrencyRatesService {
  Mono<CurrencyRatesResponse> getCurrencyRates();
}