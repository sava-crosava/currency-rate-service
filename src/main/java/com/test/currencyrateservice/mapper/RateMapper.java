package com.test.currencyrateservice.mapper;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateEntity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RateMapper {

  @Mapping(target = "type", constant = "FIAT")
  @Mapping(target = "currency", source = "dto.currency")
  @Mapping(target = "rate", source = "dto.rate")
  @Mapping(target = "fetchedAt", expression = "java(fetchedAt)")
  RateEntity toEntity(FiatRateDto dto, OffsetDateTime fetchedAt);

  @Mapping(target = "type", constant = "CRYPTO")
  @Mapping(target = "currency", source = "dto.name")
  @Mapping(target = "rate", source = "dto.value")
  @Mapping(target = "fetchedAt", expression = "java(fetchedAt)")
  RateEntity toEntity(CryptoRateDto dto, OffsetDateTime fetchedAt);

  @Mapping(target = "currency", source = "e.currency")
  @Mapping(target = "rate", source = "e.rate")
  RateItem toRateItem(RateEntity e);

  default RateItem toRateItem(String currency, BigDecimal rate) {
    return new RateItem(currency, rate);
  }
}
