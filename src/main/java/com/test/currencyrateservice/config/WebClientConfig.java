package com.test.currencyrateservice.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(RatesClientProperties.class)
@RequiredArgsConstructor
public class WebClientConfig {

  private final RatesClientProperties props;

  @Bean
  WebClient webClient() {
    Duration timeout = props.getHttp().getTimeout();
    HttpClient httpClient =
            HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(timeout.toMillis()))
                    .responseTimeout(timeout);

    ExchangeStrategies strategies =
            ExchangeStrategies.builder()
                    .codecs(c -> c.defaultCodecs().maxInMemorySize(1_048_576))
                    .build();

    return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .build();
  }
}
