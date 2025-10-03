package com.test.currencyrateservice.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rates")
public record RatesClientProperties(
        @Valid @NotNull Fiat fiat,
        @Valid @NotNull Crypto crypto,
        @Valid @NotNull Http http
) {
  public record Fiat(
          @NotBlank String baseUrl,
          @NotBlank String apiKey,
          @NotBlank String apiHeader
  ) {}

  public record Crypto(
          @NotBlank String baseUrl
  ) {}

  public record Http(
          @NotNull Duration timeout,
          @NotNull @DefaultValue("1MB") DataSize maxInMemorySize,
          @DefaultValue("false") boolean wiretapEnabled
  ) {}
}
