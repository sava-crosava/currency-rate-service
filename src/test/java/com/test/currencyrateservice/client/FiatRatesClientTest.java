package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.FiatRateDto;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class FiatRatesClientTest {

  MockWebServer server;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  @Test
  void sendsHeaderAndParsesResponse() throws Exception {
    server.enqueue(new MockResponse()
        .setBody("[{\"currency\":\"USD\",\"rate\":10.5},{\"currency\":\"EUR\",\"rate\":11.1}]")
        .addHeader("Content-Type", "application/json"));

    WebClient wc = WebClient.builder()
        .baseUrl(server.url("/fiat-currency-rates").toString())
        .defaultHeader("X-API-KEY", "secret-key")
        .build();

    FiatRatesClient client = new FiatRatesClient(wc);

    StepVerifier.create(client.getRates())
        .assertNext(list -> assertThat(list).extracting(FiatRateDto::currency).containsExactly("USD", "EUR"))
        .verifyComplete();

    RecordedRequest req = server.takeRequest();
    assertThat(req.getHeader("X-API-KEY")).isEqualTo("secret-key");
  }
}
