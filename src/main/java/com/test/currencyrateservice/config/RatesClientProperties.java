package com.test.currencyrateservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "rates")
public class RatesClientProperties {

  @NotNull
  private Fiat fiat;

  @NotNull
  private Crypto crypto;

  @NotNull
  private Http http;

  @Getter
  @Setter
  public static class Fiat {
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String apiKey;
    @NotBlank
    private String apiHeader = "X-API-KEY";
  }

  @Getter
  @Setter
  public static class Crypto {
    @NotBlank
    private String baseUrl;
  }

  @Getter
  @Setter
  public static class Http {
    @NotNull
    private Duration timeout;
    @NotNull
    private DataSize maxInMemorySize = DataSize.ofMegabytes(1);
    private boolean wiretapEnabled = false;
  }
}
