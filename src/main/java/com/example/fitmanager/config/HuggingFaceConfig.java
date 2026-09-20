package com.example.fitmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class HuggingFaceConfig {

    @Bean
    public RestClient huggingFaceRestClient( //
            final @Value("${huggingface.base-url}") String baseUrl, //
            final @Value("${huggingface.api-token}") String apiKey) {

        return RestClient.builder() //
                .baseUrl(baseUrl) //
                .defaultHeader("Authorization", "Bearer " + apiKey) //
                .build();
    }
}
