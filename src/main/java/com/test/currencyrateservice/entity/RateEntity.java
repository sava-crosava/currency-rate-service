package com.test.currencyrateservice.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("rates")
public class RateEntity {
    @Id
    private Long id;

    @Column("rate_type")
    private RateType type;

    private String currency;

    private BigDecimal rate;

    @Column("fetched_at")
    private OffsetDateTime fetchedAt;
}
