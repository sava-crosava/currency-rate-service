package com.test.currencyrateservice.service;

import com.test.currencyrateservice.client.CryptoRatesClient;
import com.test.currencyrateservice.client.FiatRatesClient;
import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import com.test.currencyrateservice.repository.RateRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CurrencyRatesServiceImplTest {

    FiatRatesClient fiat = mock(FiatRatesClient.class);
    CryptoRatesClient crypto = mock(CryptoRatesClient.class);
    RateRepository repo = mock(RateRepository.class);
    CurrencyRatesServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CurrencyRatesServiceImpl(fiat, crypto, repo);
    }

    @Test
    void bothSourcesOk_persistAndReturnFresh() {
        when(fiat.getRates()).thenReturn(Mono.just(List.of(
                new FiatRateDto("USD", new BigDecimal("10.0")),
                new FiatRateDto("EUR", new BigDecimal("11.0"))
        )));
        when(crypto.getRates()).thenReturn(Mono.just(List.of(
                new CryptoRateDto("BTC", new BigDecimal("100.0")),
                new CryptoRateDto("ETH", new BigDecimal("50.0"))
        )));
        when(repo.saveAll(anyList())).thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));

        StepVerifier.create(service.getCurrencyRates())
                .assertNext(res -> {
                    assertThat(res.fiat()).hasSize(2);
                    assertThat(res.crypto()).hasSize(2);
                })
                .verifyComplete();

        verify(repo, times(2)).saveAll(anyList());
        verify(repo, never()).findLatestPerCurrencyByType(any());
    }

    @Test
    void fiatFails_fallbackFromDb() {
        when(fiat.getRates()).thenReturn(Mono.error(new RuntimeException("fiat down")));
        when(crypto.getRates()).thenReturn(Mono.just(List.of(new CryptoRateDto("BTC", new BigDecimal("1")))));
        when(repo.saveAll(anyList())).thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));

        OffsetDateTime t2 = OffsetDateTime.now();
        RateEntity usd2 = RateEntity.builder().type(RateType.FIAT).currency("USD").rate(new BigDecimal("12")).fetchedAt(t2).build();
        RateEntity eur = RateEntity.builder().type(RateType.FIAT).currency("EUR").rate(new BigDecimal("9")).fetchedAt(t2).build();
        when(repo.findLatestPerCurrencyByType(RateType.FIAT)).thenReturn(Flux.just(usd2, eur));

        StepVerifier.create(service.getCurrencyRates())
                .assertNext(res -> {
                    assertThat(res.fiat()).extracting(com.test.currencyrateservice.dto.RateItem::currency)
                            .containsExactlyInAnyOrder("USD", "EUR");
                    assertThat(res.crypto()).hasSize(1);
                })
                .verifyComplete();

        verify(repo).findLatestPerCurrencyByType(RateType.FIAT);
    }

    @Test
    void bothFail_dbEmpty_returnsEmptySections() {
        when(fiat.getRates()).thenReturn(Mono.error(new RuntimeException("fiat down")));
        when(crypto.getRates()).thenReturn(Mono.error(new RuntimeException("crypto down")));
        when(repo.findLatestPerCurrencyByType(RateType.FIAT)).thenReturn(Flux.empty());
        when(repo.findLatestPerCurrencyByType(RateType.CRYPTO)).thenReturn(Flux.empty());

        StepVerifier.create(service.getCurrencyRates())
                .assertNext(res -> {
                    assertThat(res.fiat()).isEmpty();
                    assertThat(res.crypto()).isEmpty();
                })
                .verifyComplete();

        verify(repo, never()).saveAll(anyList());
    }

    @Test
    void emptyNetworkLists_doNotPersist() {
        when(fiat.getRates()).thenReturn(Mono.just(List.of()));
        when(crypto.getRates()).thenReturn(Mono.just(List.of()));

        StepVerifier.create(service.getCurrencyRates())
                .assertNext(res -> {
                    assertThat(res.fiat()).isEmpty();
                    assertThat(res.crypto()).isEmpty();
                })
                .verifyComplete();

        verify(repo, never()).saveAll(anyList());
        verify(repo, never()).findLatestPerCurrencyByType(any());
    }
}
