package com.test.currencyrateservice.service;

import com.test.currencyrateservice.client.CryptoRatesClient;
import com.test.currencyrateservice.client.FiatRatesClient;
import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import com.test.currencyrateservice.mapper.RateMappers;
import com.test.currencyrateservice.repository.RateRepository;
import java.time.OffsetDateTime;
import java.util.List;
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

        Mono<List<RateItem>> fiatFlow = fetchFlow(
                fiatClient.getRates().map(list -> list.stream().map(dto -> RateMappers.toEntity(dto, now)).toList()),
                RateType.FIAT,
                "fiat"
        );

        Mono<List<RateItem>> cryptoFlow = fetchFlow(
                cryptoClient.getRates().map(list -> list.stream().map(dto -> RateMappers.toEntity(dto, now)).toList()),
                RateType.CRYPTO,
                "crypto"
        );

        return Mono.zip(fiatFlow, cryptoFlow)
                .map(t -> new CurrencyRatesResponse(t.getT1(), t.getT2()))
                .doOnSuccess(r -> log.info("fetch_currency_rates done fiat={} crypto={}", r.fiat().size(), r.crypto().size()))
                .onErrorReturn(new CurrencyRatesResponse(List.of(), List.of()));
    }

    private Mono<List<RateItem>> fetchFlow(Mono<List<RateEntity>> upstream, RateType type, String name) {
        return upstream
                .doOnSubscribe(s -> log.info("fetch_{} request", name))
                .flatMap(entities -> saveAll(entities).then(Mono.just(entities)))
                .doOnNext(list -> log.info("fetch_{} success size={}", name, list.size()))
                .map(list -> list.stream().map(RateMappers::toRateItem).toList())
                .onErrorResume(ex -> {
                    log.warn("fetch_{} error fallback_db reason={}", name, ex.toString());
                    return rateRepository
                            .findLatestPerCurrencyByType(type)
                            .map(RateMappers::toRateItem)
                            .collectList()
                            .doOnNext(list -> log.info("fallback_{}_db size={}", name, list.size()));
                });
    }

    private Mono<Void> saveAll(List<RateEntity> entities) {
        if (entities.isEmpty()) return Mono.empty();
        return rateRepository
                .saveAll(entities)
                .count()
                .doOnNext(c -> log.debug("persist_rates saved={}", c))
                .then();
    }
}
