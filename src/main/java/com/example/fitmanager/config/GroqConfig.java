package com.example.fitmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class GroqConfig {

    @Bean
    public RestClient groqRestClient( //
            final @Value("${groq.base-url}") String baseUrl, //
            final @Value("${groq.api-token}") String apiToken) {

        return RestClient.builder() //
                .baseUrl(baseUrl) //
                .defaultHeader("Authorization", "Bearer " + apiToken) //
                .build();
    }
}
