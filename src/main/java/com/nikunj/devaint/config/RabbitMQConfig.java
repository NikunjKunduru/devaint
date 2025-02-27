package com.nikunj.devaint.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String REQUEST_QUEUE = "gpt4o_request_queue";
    public static final String RESPONSE_QUEUE = "gpt4o_response_queue";
    public static final String DLX_EXCHANGE = "gpt4o_dlx_exchange";
    public static final String DLQ = "gpt4o_dlq";

    // Retry Queues with increasing delay (Exponential Backoff)
    public static final String RETRY_QUEUE_1 = "gpt4o_retry_queue_5s";
    public static final String RETRY_QUEUE_2 = "gpt4o_retry_queue_15s";
    public static final String RETRY_QUEUE_3 = "gpt4o_retry_queue_30s";

    // Constants for RabbitMQ arguments
    public static final String DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
    public static final String MESSAGE_TTL = "x-message-ttl";

    @Bean
    public Queue requestQueue() {
        return QueueBuilder.durable(REQUEST_QUEUE)
                .withArgument(DEAD_LETTER_EXCHANGE, DLX_EXCHANGE) // Send failed messages to DLX
                .build();
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ, true);
    }

    @Bean
    public Queue retryQueue1() {
        return QueueBuilder.durable(RETRY_QUEUE_1)
                .withArgument(DEAD_LETTER_EXCHANGE, "")
                .withArgument(DEAD_LETTER_ROUTING_KEY, REQUEST_QUEUE)
                .withArgument(MESSAGE_TTL, 5000) // 5 sec delay
                .build();
    }

    @Bean
    public Queue retryQueue2() {
        return QueueBuilder.durable(RETRY_QUEUE_2)
                .withArgument(DEAD_LETTER_EXCHANGE, "")
                .withArgument(DEAD_LETTER_ROUTING_KEY, REQUEST_QUEUE)
                .withArgument(MESSAGE_TTL, 15000) // 15 sec delay
                .build();
    }

    @Bean
    public Queue retryQueue3() {
        return QueueBuilder.durable(RETRY_QUEUE_3)
                .withArgument(DEAD_LETTER_EXCHANGE, "")
                .withArgument(DEAD_LETTER_ROUTING_KEY, DLQ) // Move to DLQ if max retries exceeded
                .withArgument(MESSAGE_TTL, 30000) // 30 sec delay
                .build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}