package br.com.atocf.pedidoprocessor.config;

import br.com.atocf.pedidoprocessor.service.RabbitMQConsumer;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public RabbitMQConsumer mockRabbitMQConsumer() {
        return Mockito.mock(RabbitMQConsumer.class);
    }
}