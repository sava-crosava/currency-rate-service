package com.test.currencyrateservice.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import com.test.currencyrateservice.client.model.CryptoRateDto;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class CryptoRatesClientTest {

  MockWebServer server;
  CryptoRatesClient client;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
    WebClient wc = WebClient.builder().baseUrl(server.url("/").toString()).build();
    client = new CryptoRatesClient(wc);
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void ok_parsesFlux() {
    server.enqueue(new MockResponse().setBody("[{\"name\":\"BTC\",\"value\":100},{\"name\":\"ETH\",\"value\":200}]").addHeader("Content-Type","application/json"));
    StepVerifier.create(client.getRates().map(CryptoRateDto::name))
        .expectNext("BTC","ETH")
        .verifyComplete();
  }

  @Test
  void error_status_propagates() {
    server.enqueue(new MockResponse().setResponseCode(502));
    StepVerifier.create(client.getRates())
        .expectError()
        .verify();
  }
}
