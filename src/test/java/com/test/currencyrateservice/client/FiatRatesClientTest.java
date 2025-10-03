package com.test.currencyrateservice.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import com.test.currencyrateservice.client.model.FiatRateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class FiatRatesClientTest {

  MockWebServer server;
  FiatRatesClient client;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    WebClient wc = WebClient.builder().baseUrl(server.url("/").toString()).build();
    client = new FiatRatesClient(wc);
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void ok_parsesFlux() {
    server.enqueue(new MockResponse().setBody("[{\"currency\":\"USD\",\"rate\":1.1},{\"currency\":\"EUR\",\"rate\":2.2}]").addHeader("Content-Type","application/json"));
    StepVerifier.create(client.getRates().map(FiatRateDto::currency))
        .expectNext("USD","EUR")
        .verifyComplete();
  }

  @Test
  void error_status_propagates() {
    server.enqueue(new MockResponse().setResponseCode(500));
    StepVerifier.create(client.getRates())
        .expectError()
        .verify();
  }
}
