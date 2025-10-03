package com.test.currencyrateservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class RatesClientPropertiesValidationTest {

  ApplicationContextRunner runner = new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class,
                  ValidationAutoConfiguration.class
          ))
          .withUserConfiguration(BindConfig.class);

  @Test
  void valid_properties_bind() {
    runner.withPropertyValues(
            "rates.fiat.base-url=http://x",
            "rates.fiat.api-key=k",
            "rates.fiat.api-header=H",
            "rates.crypto.base-url=http://y",
            "rates.http.timeout=1s",
            "rates.http.max-in-memory-size=64KB",
            "rates.http.wiretap-enabled=false"
    ).run(ctx -> assertThat(ctx).hasNotFailed());
  }

  @Test
  void missing_required_fields_fail_binding() {
    runner.withPropertyValues(
            "rates.fiat.base-url=",
            "rates.fiat.api-key=",
            "rates.fiat.api-header=",
            "rates.crypto.base-url=",
            "rates.http.timeout=1s"
    ).run(ctx -> assertThat(ctx).hasFailed());
  }

  @Configuration
  @EnableConfigurationProperties(RatesClientProperties.class)
  static class BindConfig {}
}
