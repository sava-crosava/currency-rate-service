package com.test.currencyrateservice.mapper;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class RateMappers {
  private RateMappers() {}

  public static RateEntity toEntity(FiatRateDto dto, OffsetDateTime fetchedAt) {
    return RateEntity.builder()
            .type(RateType.FIAT)
            .currency(dto.currency())
            .rate(dto.rate())
            .fetchedAt(fetchedAt)
            .build();
  }

  public static RateEntity toEntity(CryptoRateDto dto, OffsetDateTime fetchedAt) {
    return RateEntity.builder()
            .type(RateType.CRYPTO)
            .currency(dto.name())
            .rate(dto.value())
            .fetchedAt(fetchedAt)
            .build();
  }

  public static RateItem toRateItem(RateEntity e) {
    return new RateItem(e.getCurrency(), e.getRate());
  }

  public static RateItem toRateItem(String currency, BigDecimal rate) {
    return new RateItem(currency, rate);
  }
}
