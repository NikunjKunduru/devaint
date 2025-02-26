package com.nikunj.devaint.service.rabbitmq;

import com.nikunj.devaint.config.RabbitMQConfig;
import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class Gpt4oRequestProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentHashMap<String, String> requestMap = new ConcurrentHashMap<>();

    public Gpt4oRequestProducer(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    public String sendRequestToQueue(String eventId, Gpt4oQueueDTO queueRequest) {
        String methodName = "sendRequestToQueue()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug("Preparing to send request for Event ID: {}", eventId);

        try {
            String correlationId = UUID.randomUUID().toString();
            requestMap.put(correlationId, eventId);
            log.info("Generated Correlation ID: {} for Event ID: {}", correlationId, eventId);

            rabbitTemplate.convertAndSend(RabbitMQConfig.REQUEST_QUEUE, queueRequest, msg -> {
                msg.getMessageProperties().setCorrelationId(correlationId);
                msg.getMessageProperties().setReplyTo(RabbitMQConfig.RESPONSE_QUEUE);
                return msg;
            });

            log.info("Request successfully sent to queue: {} with Correlation ID: {}", RabbitMQConfig.REQUEST_QUEUE, correlationId);
            log.info(LogConstants.END_METHOD, methodName);
            return correlationId;
        } catch (Exception e) {
            log.error("Exception in {} for Event ID {}: {}", methodName, eventId, e.getMessage(), e);
            return null;
        }
    }
}
