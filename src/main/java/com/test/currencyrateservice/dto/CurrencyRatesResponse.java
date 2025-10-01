package com.test.currencyrateservice.dto;

import java.util.List;

public record CurrencyRatesResponse(List<RateItem> fiat, List<RateItem> crypto) {}