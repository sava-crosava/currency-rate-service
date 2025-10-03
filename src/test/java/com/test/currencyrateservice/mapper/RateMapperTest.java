package com.test.currencyrateservice.mapper;

import com.test.currencyrateservice.client.model.CryptoRateDto;
import com.test.currencyrateservice.client.model.FiatRateDto;
import com.test.currencyrateservice.dto.RateItem;
import com.test.currencyrateservice.entity.RateType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RateMapperTest {

    RateMapper mapper = Mappers.getMapper(RateMapper.class);

    @Test
    void fiatDto_toEntity_and_back() {
        var now = OffsetDateTime.now();
        var dto = new FiatRateDto("USD", new BigDecimal("1.23"));
        var e = mapper.toEntity(dto, now);
        assertThat(e.getType()).isEqualTo(RateType.FIAT);
        assertThat(e.getCurrency()).isEqualTo("USD");
        assertThat(e.getRate()).isEqualByComparingTo("1.23");
        assertThat(e.getFetchedAt()).isEqualTo(now);
        RateItem item = mapper.toRateItem(e);
        assertThat(item.currency()).isEqualTo("USD");
        assertThat(item.rate()).isEqualByComparingTo("1.23");
    }

    @Test
    void cryptoDto_toEntity_and_back() {
        var now = OffsetDateTime.now();
        var dto = new CryptoRateDto("BTC", new BigDecimal("55555.55"));
        var e = mapper.toEntity(dto, now);
        assertThat(e.getType()).isEqualTo(RateType.CRYPTO);
        assertThat(e.getCurrency()).isEqualTo("BTC");
        assertThat(e.getRate()).isEqualByComparingTo("55555.55");
        assertThat(e.getFetchedAt()).isEqualTo(now);
        RateItem item = mapper.toRateItem(e);
        assertThat(item.currency()).isEqualTo("BTC");
    }
}
