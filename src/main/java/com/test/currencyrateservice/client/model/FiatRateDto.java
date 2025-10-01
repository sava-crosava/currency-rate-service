package com.test.currencyrateservice.client.model;

import java.math.BigDecimal;

public record FiatRateDto(String currency, BigDecimal rate) {}