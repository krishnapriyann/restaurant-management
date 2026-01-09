package io.poc.paymentservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Config {

    private final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean
    public WebClient webClient() {
        log.info("WebClient");
        return WebClient.builder()
                .build();
    }
}
