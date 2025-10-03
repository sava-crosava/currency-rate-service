package com.test.currencyrateservice.config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTest {

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
  void fiat_client_adds_api_header_and_baseUrl() throws InterruptedException {
    var props = new RatesClientProperties(
        new RatesClientProperties.Fiat(server.url("/fiat").toString(), "k", "X-API-KEY"),
        new RatesClientProperties.Crypto(server.url("/crypto").toString()),
        new RatesClientProperties.Http(Duration.ofSeconds(5), org.springframework.util.unit.DataSize.ofKilobytes(256), false)
    );
    var cfg = new WebClientConfig(props);
    WebClient.Builder builder = cfg.commonWebClientBuilder();
    WebClient fiat = cfg.fiatWebClient(builder);
    server.enqueue(new MockResponse().setBody("[]").addHeader("Content-Type","application/json"));
    Mono.from(fiat.get().retrieve().bodyToFlux(Object.class)).blockOptional();
    var req = server.takeRequest();
    assertThat(req.getPath()).startsWith("/fiat");
    assertThat(req.getHeader("X-API-KEY")).isEqualTo("k");
  }

  @Test
  void crypto_client_uses_baseUrl() throws InterruptedException {
    var props = new RatesClientProperties(
        new RatesClientProperties.Fiat(server.url("/fiat").toString(), "k", "X"),
        new RatesClientProperties.Crypto(server.url("/crypto").toString()),
        new RatesClientProperties.Http(Duration.ofSeconds(5), org.springframework.util.unit.DataSize.ofKilobytes(256), false)
    );
    var cfg = new WebClientConfig(props);
    WebClient.Builder builder = cfg.commonWebClientBuilder();
    WebClient crypto = cfg.cryptoWebClient(builder);
    server.enqueue(new MockResponse().setBody("[]").addHeader("Content-Type","application/json"));
    Mono.from(crypto.get().retrieve().bodyToFlux(Object.class)).blockOptional();
    var req = server.takeRequest();
    assertThat(req.getPath()).startsWith("/crypto");
  }
}
