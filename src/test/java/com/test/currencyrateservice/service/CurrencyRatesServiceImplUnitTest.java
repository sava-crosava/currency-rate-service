package com.test.currencyrateservice.service;

import com.test.currencyrateservice.client.CryptoRatesClient;
import com.test.currencyrateservice.client.FiatRatesClient;
import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import com.test.currencyrateservice.mapper.RateMapper;
import com.test.currencyrateservice.repository.RateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

class CurrencyRatesServiceImplUnitTest {

  FiatRatesClient fiat;
  CryptoRatesClient crypto;
  RateRepository repo;
  RateMapper mapper;
  CurrencyRatesServiceImpl service;

  @BeforeEach
  void setUp() {
    fiat = Mockito.mock(FiatRatesClient.class);
    crypto = Mockito.mock(CryptoRatesClient.class);
    repo = Mockito.mock(RateRepository.class);
    mapper = Mappers.getMapper(RateMapper.class);
    service = new CurrencyRatesServiceImpl(fiat, mapper, crypto, repo);
  }

  @Test
  void both_sources_ok_and_persisted() {
    when(fiat.getRates()).thenReturn(Flux.just(new FiatRateDto("USD", new BigDecimal("1.1"))));
    when(crypto.getRates()).thenReturn(Flux.just(new CryptoRateDto("BTC", new BigDecimal("2.2"))));
    when(repo.saveAll(Mockito.<Publisher<RateEntity>>any()))
            .thenAnswer(inv -> Flux.from(inv.getArgument(0)));
    StepVerifier.create(service.getCurrencyRates())
            .expectNextMatches(r -> r.fiat().size()==1 && r.crypto().size()==1)
            .verifyComplete();
  }

  @Test
  void fiat_error_fallback_from_db_crypto_ok() {
    when(fiat.getRates()).thenReturn(Flux.error(new RuntimeException("fiat fail")));
    when(crypto.getRates()).thenReturn(Flux.just(new CryptoRateDto("ETH", new BigDecimal("3.3"))));
    when(repo.saveAll(Mockito.<Publisher<RateEntity>>any()))
            .thenReturn(Flux.error(new RuntimeException("save fail")));
    when(repo.findLatestPerCurrencyByType(RateType.FIAT))
            .thenReturn(Flux.just(new RateEntity(10L, RateType.FIAT, "USD", new BigDecimal("1.0"), OffsetDateTime.now())));
    when(repo.findLatestPerCurrencyByType(RateType.CRYPTO))
            .thenReturn(Flux.just(new RateEntity(11L, RateType.CRYPTO, "ETH", new BigDecimal("3.3"), OffsetDateTime.now())));
    StepVerifier.create(service.getCurrencyRates())
            .expectNextMatches(r -> r.fiat().size()==1 && r.crypto().size()==1
                    && r.fiat().getFirst().currency().equals("USD")
                    && r.crypto().getFirst().currency().equals("ETH"))
            .verifyComplete();
  }

  @Test
  void both_sources_empty_returns_empty_lists() {
    when(fiat.getRates()).thenReturn(Flux.empty());
    when(crypto.getRates()).thenReturn(Flux.empty());
    when(repo.saveAll(Mockito.<Publisher<RateEntity>>any()))
            .thenAnswer(inv -> Flux.from(inv.getArgument(0)));
    StepVerifier.create(service.getCurrencyRates())
            .expectNextMatches(r -> r.fiat().isEmpty() && r.crypto().isEmpty())
            .verifyComplete();
  }

  @Test
  void save_error_triggers_fallback_for_corresponding_type() {
    when(fiat.getRates()).thenReturn(Flux.just(new FiatRateDto("EUR", new BigDecimal("1.2"))));
    when(crypto.getRates()).thenReturn(Flux.just(new CryptoRateDto("BTC", new BigDecimal("9"))));
    when(repo.saveAll(Mockito.<Publisher<RateEntity>>any()))
            .thenReturn(Flux.error(new RuntimeException("db down")));
    when(repo.findLatestPerCurrencyByType(RateType.FIAT))
            .thenReturn(Flux.fromIterable(List.of(new RateEntity(1L, RateType.FIAT, "EUR", new BigDecimal("1.0"), OffsetDateTime.now()))));
    when(repo.findLatestPerCurrencyByType(RateType.CRYPTO))
            .thenReturn(Flux.fromIterable(List.of(new RateEntity(2L, RateType.CRYPTO, "BTC", new BigDecimal("8"), OffsetDateTime.now()))));
    StepVerifier.create(service.getCurrencyRates())
            .expectNextMatches(r -> r.fiat().getFirst().rate().compareTo(new BigDecimal("1.0"))==0
                    && r.crypto().getFirst().rate().compareTo(new BigDecimal("8"))==0)
            .verifyComplete();
  }
}
