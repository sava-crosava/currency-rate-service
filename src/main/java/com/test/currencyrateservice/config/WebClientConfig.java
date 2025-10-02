package com.test.currencyrateservice.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(RatesClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {

  private final RatesClientProperties props;

  @Bean
  WebClient.Builder commonWebClientBuilder() {
    Duration timeout = props.getHttp().getTimeout();
    HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(timeout.toMillis()))
            .responseTimeout(timeout)
            .wiretap(props.getHttp().isWiretapEnabled());

    ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(Math.toIntExact(props.getHttp().getMaxInMemorySize().toBytes())))
            .build();

    return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .filter(errorOnStatus())
            .filter(logRequest())
            .filter(logResponse())
            .filter(latency());
  }

  @Bean("fiatWebClient")
  WebClient fiatWebClient(WebClient.Builder builder) {
    return builder
            .baseUrl(props.getFiat().getBaseUrl())
            .defaultHeader(props.getFiat().getApiHeader(), props.getFiat().getApiKey())
            .build();
  }

  @Bean("cryptoWebClient")
  WebClient cryptoWebClient(WebClient.Builder builder) {
    return builder
            .baseUrl(props.getCrypto().getBaseUrl())
            .build();
  }

  private ExchangeFilterFunction errorOnStatus() {
    return (request, next) ->
            next.exchange(request).flatMap(response -> {
              if (response.statusCode().isError()) {
                return response.createException().flatMap(Mono::error);
              }
              return Mono.just(response);
            });
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(req -> {
      boolean hasApiKey = req.headers().containsKey(props.getFiat().getApiHeader());
      log.info("HTTP {} {} apiKey: {}", req.method(), req.url(), hasApiKey ? "***" : "absent");
      return Mono.just(req);
    });
  }

  private ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(res -> {
      int code = res.statusCode().value();
      String ct = res.headers().asHttpHeaders().getFirst("Content-Type");
      log.info("HTTP <- {} {}", code, ct);
      return Mono.just(res);
    });
  }

  private ExchangeFilterFunction latency() {
    return (req, next) -> {
      long start = System.nanoTime();
      return next.exchange(req)
              .doOnNext(res -> {
                long ms = Duration.ofNanos(System.nanoTime() - start).toMillis();
                log.debug("HTTP latency {} {} {}ms", req.method(), req.url(), ms);
              });
    };
  }
}
