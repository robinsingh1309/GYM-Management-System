package com.example.fitmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class EmbeddingConfig {

    @Bean
    public RestClient embeddingRestClient( //
            final @Value("${embedding.base-url}") String baseUrl) {

        return RestClient.builder() //
                .baseUrl(baseUrl) //
                .build();
    }
}
