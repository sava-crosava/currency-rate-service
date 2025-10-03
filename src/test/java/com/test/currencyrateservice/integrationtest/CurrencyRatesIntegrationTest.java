package com.test.currencyrateservice.integrationtest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import com.test.currencyrateservice.PostgresContainerBase;
import com.test.currencyrateservice.service.CurrencyRatesService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class CurrencyRatesIntegrationTest extends PostgresContainerBase {

    static MockWebServer fiat;
    static MockWebServer crypto;

    @DynamicPropertySource
    static void endpoints(DynamicPropertyRegistry r) {
        r.add("rates.fiat.base-url", () -> fiat.url("/fiat").toString());
        r.add("rates.fiat.api-key", () -> "k");
        r.add("rates.fiat.api-header", () -> "X-API-KEY");
        r.add("rates.crypto.base-url", () -> crypto.url("/crypto").toString());
        r.add("rates.http.timeout", () -> "5s");
    }

    @BeforeAll
    static void start() throws IOException {
        fiat = new MockWebServer();
        crypto = new MockWebServer();
        fiat.start();
        crypto.start();
    }

    @AfterAll
    static void stop() throws IOException {
        fiat.shutdown();
        crypto.shutdown();
    }

    @Autowired
    CurrencyRatesService service;

    @Test
    void both_ok_persist_and_return() throws Exception {
        fiat.enqueue(new MockResponse().setBody("[{\"currency\":\"USD\",\"rate\":1.1}]").addHeader("Content-Type","application/json"));
        crypto.enqueue(new MockResponse().setBody("[{\"name\":\"BTC\",\"value\":2.2}]").addHeader("Content-Type","application/json"));
        StepVerifier.create(service.getCurrencyRates())
                .expectNextMatches(r -> r.fiat().size()==1 && r.crypto().size()==1)
                .verifyComplete();
        fiat.takeRequest(2, TimeUnit.SECONDS);
        crypto.takeRequest(2, TimeUnit.SECONDS);
    }

    @Test
    void fiat_error_crypto_ok_fallback_from_db_for_fiat() throws Exception {
        fiat.enqueue(new MockResponse().setResponseCode(500));
        crypto.enqueue(new MockResponse().setBody("[{\"name\":\"ETH\",\"value\":3.3}]").addHeader("Content-Type","application/json"));
        StepVerifier.create(service.getCurrencyRates())
                .expectNextMatches(r -> r.crypto().size()==1 && r.fiat()!=null)
                .verifyComplete();
        crypto.takeRequest(2, TimeUnit.SECONDS);
    }

    @Test
    void both_empty_returns_empty_lists() {
        fiat.enqueue(new MockResponse().setBody("[]").addHeader("Content-Type","application/json"));
        crypto.enqueue(new MockResponse().setBody("[]").addHeader("Content-Type","application/json"));
        StepVerifier.create(service.getCurrencyRates())
                .expectNextMatches(r -> r.fiat().isEmpty() && r.crypto().isEmpty())
                .verifyComplete();
    }
}
