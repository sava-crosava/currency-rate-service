package com.test.currencyrateservice.controller;

import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.exception.ApiExceptionHandler;
import com.test.currencyrateservice.service.CurrencyRatesService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CurrencyRatesController.class)
@Import(ApiExceptionHandler.class)
class CurrencyRatesControllerTest {

  @MockitoBean
  CurrencyRatesService service;

  @Resource
  WebTestClient client;

  @Test
  void ok_returns_payload() {
    var resp = new CurrencyRatesResponse(
            List.of(new RateItem("USD", new BigDecimal("1.1"))),
            List.of(new RateItem("BTC", new BigDecimal("2.2")))
    );
    when(service.getCurrencyRates()).thenReturn(Mono.just(resp));
    client.get().uri("/currency-rates")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.fiat[0].currency").isEqualTo("USD")
            .jsonPath("$.crypto[0].currency").isEqualTo("BTC");
  }

  @Test
  void unhandled_error_is_caught_by_global_handler() {
    when(service.getCurrencyRates()).thenReturn(Mono.error(new RuntimeException("boom")));
    client.get().uri("/currency-rates")
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .jsonPath("$.status").isEqualTo(500)
            .jsonPath("$.error").exists()
            .jsonPath("$.path").isEqualTo("/currency-rates");
  }
}
