package com.nikunj.devaint.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String REQUEST_QUEUE = "gpt4o_request_queue";
    public static final String RESPONSE_QUEUE = "gpt4o_response_queue";

    @Bean
    public Queue requestQueue() {
        return new Queue(REQUEST_QUEUE, true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(RESPONSE_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
