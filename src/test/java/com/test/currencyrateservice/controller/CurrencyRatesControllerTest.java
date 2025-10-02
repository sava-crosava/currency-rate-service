package com.test.currencyrateservice.controller;

import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.service.CurrencyRatesService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CurrencyRatesController.class)
class CurrencyRatesControllerTest {

  @Autowired
  WebTestClient webTestClient;

  @MockitoBean
  CurrencyRatesService service;

  @Test
  void getCurrencyRates_returns200AndBody() {
    CurrencyRatesResponse resp = new CurrencyRatesResponse(
            List.of(new RateItem("USD", new BigDecimal("10.0"))),
            List.of(new RateItem("BTC", new BigDecimal("100.0")))
    );
    when(service.getCurrencyRates()).thenReturn(Mono.just(resp));

    webTestClient.get().uri("/currency-rates")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith("application/json")
            .expectBody()
            .jsonPath("$.fiat[0].currency").isEqualTo("USD")
            .jsonPath("$.crypto[0].currency").isEqualTo("BTC");
  }
}
