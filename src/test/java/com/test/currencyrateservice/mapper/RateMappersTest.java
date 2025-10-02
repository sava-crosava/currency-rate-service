package com.test.currencyrateservice.mapper;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateEntity;
import com.test.currencyrateservice.entity.RateType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateMappersTest {

  @Test
  void fiatDtoToEntity_andBackToItem() {
    var dto = new FiatRateDto("USD", new BigDecimal("10"));
    var now = OffsetDateTime.now();
    RateEntity e = RateMappers.toEntity(dto, now);
    assertThat(e.getType()).isEqualTo(RateType.FIAT);
    assertThat(e.getCurrency()).isEqualTo("USD");
    assertThat(e.getRate()).isEqualTo(new BigDecimal("10"));
    RateItem item = RateMappers.toRateItem(e);
    assertThat(item.currency()).isEqualTo("USD");
    assertThat(item.rate()).isEqualTo(new BigDecimal("10"));
  }

  @Test
  void cryptoDtoToEntity_andBackToItem() {
    var dto = new CryptoRateDto("BTC", new BigDecimal("1"));
    var now = OffsetDateTime.now();
    RateEntity e = RateMappers.toEntity(dto, now);
    assertThat(e.getType()).isEqualTo(RateType.CRYPTO);
    assertThat(e.getCurrency()).isEqualTo("BTC");
  }
}
