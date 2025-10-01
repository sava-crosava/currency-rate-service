package com.test.currencyrateservice.service;

import com.test.currencyrateservice.client.CryptoRatesClient;
import com.test.currencyrateservice.client.FiatRatesClient;
import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.mapper.RateMappers;
import com.test.currencyrateservice.repository.RateRepository;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRatesServiceImpl implements CurrencyRatesService {

    private final FiatRatesClient fiatClient;
    private final CryptoRatesClient cryptoClient;
    private final RateRepository rateRepository;

    @Override
    public Mono<CurrencyRatesResponse> getCurrencyRates() {
        OffsetDateTime now = OffsetDateTime.now();
        log.info("fetch_currency_rates start at={}", now);

        Mono<List<RateItem>> fiatFlow =
                fiatClient
                        .getRates()
                        .doOnSubscribe(s -> log.info("fetch_fiat request"))
                        .map(list -> list.stream().map(dto -> RateMappers.toEntity(dto, now)).toList())
                        .flatMap(entities -> saveAll(entities).then(Mono.just(entities)))
                        .doOnNext(list -> log.info("fetch_fiat success size={}", list.size()))
                        .map(list -> list.stream().map(RateMappers::toRateItem).toList())
                        .onErrorResume(
                                ex -> {
                                    log.warn("fetch_fiat error fallback_db reason={}", ex.toString());
                                    return rateRepository
                                            .findByTypeOrderByFetchedAtDesc("FIAT")
                                            .collectList()
                                            .map(this::latestPerCurrency)
                                            .doOnNext(list -> log.info("fallback_fiat_db size={}", list.size()));
                                });

        Mono<List<RateItem>> cryptoFlow =
                cryptoClient
                        .getRates()
                        .doOnSubscribe(s -> log.info("fetch_crypto request"))
                        .map(list -> list.stream().map(dto -> RateMappers.toEntity(dto, now)).toList())
                        .flatMap(entities -> saveAll(entities).then(Mono.just(entities)))
                        .doOnNext(list -> log.info("fetch_crypto success size={}", list.size()))
                        .map(list -> list.stream().map(RateMappers::toRateItem).toList())
                        .onErrorResume(
                                ex -> {
                                    log.warn("fetch_crypto error fallback_db reason={}", ex.toString());
                                    return rateRepository
                                            .findByTypeOrderByFetchedAtDesc("CRYPTO")
                                            .collectList()
                                            .map(this::latestPerCurrency)
                                            .doOnNext(list -> log.info("fallback_crypto_db size={}", list.size()));
                                });

        return Mono.zip(fiatFlow, cryptoFlow)
                .map(t -> new CurrencyRatesResponse(t.getT1(), t.getT2()))
                .doOnSuccess(r -> log.info("fetch_currency_rates done fiat={} crypto={}", r.fiat().size(), r.crypto().size()));
    }

    private Mono<Void> saveAll(List<RateEntity> entities) {
        if (entities.isEmpty()) return Mono.empty();
        return rateRepository.saveAll(entities).doOnSubscribe(s -> log.debug("persist_rates size={}", entities.size())).then();
    }

    private List<RateItem> latestPerCurrency(List<RateEntity> sortedDesc) {
        Map<String, RateItem> map = new LinkedHashMap<>();
        for (RateEntity e : sortedDesc) {
            if (!map.containsKey(e.getCurrency())) {
                map.put(e.getCurrency(), RateMappers.toRateItem(e));
            }
        }
        return List.copyOf(map.values());
    }
}
