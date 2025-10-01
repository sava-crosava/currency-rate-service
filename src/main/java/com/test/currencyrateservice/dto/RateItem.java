package com.test.currencyrateservice.dto;

import java.math.BigDecimal;

public record RateItem(String currency, BigDecimal rate) {}