package com.test.currencyrateservice.client;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoRatesClientTest {

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
  void parsesResponse() {
    server.enqueue(new MockResponse()
            .setBody("[{\"name\":\"BTC\",\"value\":1.0},{\"name\":\"ETH\",\"value\":2.0}]")
            .addHeader("Content-Type", "application/json"));

    WebClient wc = WebClient.builder()
            .baseUrl(server.url("/crypto-currency-rates").toString())
            .build();

    CryptoRatesClient client = new CryptoRatesClient(wc);

    StepVerifier.create(client.getRates())
            .assertNext(list -> assertThat(list).extracting(CryptoRateDto::name).containsExactly("BTC", "ETH"))
            .verifyComplete();
  }
}
