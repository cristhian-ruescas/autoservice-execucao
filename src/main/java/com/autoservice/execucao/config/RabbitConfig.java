package com.autoservice.execucao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.autoservice.execucao.messaging.TiposEvento.COMPENSAR_FILA;
import static com.autoservice.execucao.messaging.TiposEvento.DIAGNOSTICO_SOLICITADO;
import static com.autoservice.execucao.messaging.TiposEvento.EXCHANGE;
import static com.autoservice.execucao.messaging.TiposEvento.EXECUCAO_SOLICITADA;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "autoservice.execucao";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue execucaoQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding bindDiagnostico(final Queue execucaoQueue, final TopicExchange sagaExchange) {
        return BindingBuilder.bind(execucaoQueue).to(sagaExchange).with(DIAGNOSTICO_SOLICITADO);
    }

    @Bean
    public Binding bindReparo(final Queue execucaoQueue, final TopicExchange sagaExchange) {
        return BindingBuilder.bind(execucaoQueue).to(sagaExchange).with(EXECUCAO_SOLICITADA);
    }

    @Bean
    public Binding bindLiberacao(final Queue execucaoQueue, final TopicExchange sagaExchange) {
        return BindingBuilder.bind(execucaoQueue).to(sagaExchange).with(COMPENSAR_FILA);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(final ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            final ConnectionFactory connectionFactory,
            final Jackson2JsonMessageConverter converter
    ) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(EXCHANGE);
        template.setMessageConverter(converter);
        return template;
    }
}
