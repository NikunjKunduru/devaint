package com.nikunj.devaint.client;

import com.nikunj.devaint.config.RabbitMQConfig;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMqClient {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMqClient(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    public void sendProducerMessage(String queueName, Object queueRequest, String correlationId) {
        log.debug(LogConstants.MESSAGE_SENT, queueName, correlationId);
        rabbitTemplate.convertAndSend(queueName, queueRequest, msg -> {
            msg.getMessageProperties().setCorrelationId(correlationId);
            msg.getMessageProperties().setReplyTo(RabbitMQConfig.RESPONSE_QUEUE);
            return msg;
        });
    }

    public void sendConsumerMessage(String queueName, Object queueRequest, String correlationId) {
        log.debug(LogConstants.MESSAGE_SENT, queueName, correlationId);
        rabbitTemplate.convertAndSend(queueName, queueRequest, msg -> {
            msg.getMessageProperties().setCorrelationId(correlationId);
            return msg;
        });
    }

    public void sendRetryMessage(String queueName, Object queueRequest, int retryCount) {
        log.info(LogConstants.MESSAGE_SENT, queueName, "RetryCount: " + retryCount);
        rabbitTemplate.convertAndSend(queueName, queueRequest, msg -> {
            msg.getMessageProperties().setHeader("x-retry-count", retryCount);
            return msg;
        });
    }
}
