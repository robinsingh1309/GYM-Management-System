package com.example.fitmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;


@Configuration
public class QdrantConfig {

    @Bean
    public QdrantClient qdrantClient( //
            final @Value("${qdrant.host}") String host, //
            final @Value("${qdrant.grpc-port}") int grpcPort) {

        return new QdrantClient( //
                QdrantGrpcClient.newBuilder( //
                        host, grpcPort, false //
                ).build() //
        );
    }
}
