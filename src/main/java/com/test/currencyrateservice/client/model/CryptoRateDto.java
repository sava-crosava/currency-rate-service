package com.test.currencyrateservice.client.model;

import java.math.BigDecimal;

public record CryptoRateDto(String name, BigDecimal value) {}