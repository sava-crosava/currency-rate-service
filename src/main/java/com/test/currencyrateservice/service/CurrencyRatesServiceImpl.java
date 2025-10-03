package com.test.currencyrateservice.service;

import com.test.currencyrateservice.client.CryptoRatesClient;
import com.test.currencyrateservice.client.FiatRatesClient;
import com.test.currencyrateservice.dto.CurrencyRatesResponse;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import com.test.currencyrateservice.mapper.RateMapper;
import com.test.currencyrateservice.repository.RateRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRatesServiceImpl implements CurrencyRatesService {

    private final FiatRatesClient fiatClient;
    private final RateMapper rateMapper;
    private final CryptoRatesClient cryptoClient;
    private final RateRepository rateRepository;
    private final Supplier<OffsetDateTime> nowSupplier = OffsetDateTime::now;

    @Override
    public Mono<CurrencyRatesResponse> getCurrencyRates() {
        OffsetDateTime now = nowSupplier.get();
        log.info("fetch_currency_rates start at={}", now);

        Flux<RateItem> fiatFlux = fetchFlow(
                fiatClient.getRates().map(dto -> rateMapper.toEntity(dto, now)),
                RateType.FIAT
        );

        Flux<RateItem> cryptoFlux = fetchFlow(
                cryptoClient.getRates().map(dto -> rateMapper.toEntity(dto, now)),
                RateType.CRYPTO
        );

        return Mono.zip(fiatFlux.collectList(), cryptoFlux.collectList())
                .map(t -> new CurrencyRatesResponse(t.getT1(), t.getT2()))
                .doOnSuccess(r -> log.info("fetch_currency_rates done fiat={} crypto={}", r.fiat().size(), r.crypto().size()))
                .onErrorResume(e -> {
                    log.error("fetch_currency_rates unexpected error", e);
                    return Mono.just(new CurrencyRatesResponse(List.of(), List.of()));
                });
    }

    private Flux<RateItem> fetchFlow(Flux<RateEntity> upstream, RateType type) {
        return rateRepository
                .saveAll(
                        upstream
                                .doOnSubscribe(s -> log.info("fetch_{} request", type))
                                .doOnComplete(() -> log.debug("fetch_{} upstream completed", type))
                )
                .doOnNext(e -> log.debug("persist_{} id={} currency={} rate={}", type, e.getId(), e.getCurrency(), e.getRate()))
                .map(rateMapper::toRateItem)
                .switchIfEmpty(Flux.defer(() -> {
                    log.info("fetch_{} empty result, no items to persist", type);
                    return Flux.empty();
                }))
                .onErrorResume(ex -> {
                    log.warn("fetch_{} error fallback_db reason={}", type, ex.toString());
                    return rateRepository
                            .findLatestPerCurrencyByType(type)
                            .map(rateMapper::toRateItem)
                            .doOnComplete(() -> log.info("fallback_{}_db completed", type));
                });
    }
}
