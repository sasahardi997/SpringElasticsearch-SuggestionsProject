package com.hardi.suggestions.config;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RestClientConfig {
    @Bean
    TestRestTemplate restTemplate() {
        return new TestRestTemplate();
    }
}
