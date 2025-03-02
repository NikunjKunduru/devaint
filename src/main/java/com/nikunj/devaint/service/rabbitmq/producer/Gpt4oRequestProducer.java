package com.nikunj.devaint.service.rabbitmq.producer;

import com.nikunj.devaint.config.RabbitMQConfig;
import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.client.RabbitMqClient;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class Gpt4oRequestProducer {

    private final RabbitMqClient rabbitMqClient;
    private final ConcurrentHashMap<String, String> requestMap = new ConcurrentHashMap<>();

    @Autowired
    public Gpt4oRequestProducer(RabbitMqClient rabbitMqClient) {
        this.rabbitMqClient = rabbitMqClient;
    }

    public String sendRequestToQueue(String eventId, Gpt4oQueueDTO queueRequest) {
        String methodName = "sendRequestToQueue()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug(LogConstants.PROCESSING_EVENT, eventId, "N/A");

        try {
            String correlationId = UUID.randomUUID().toString();
            requestMap.put(correlationId, eventId);
            log.info(LogConstants.GENERATED_CORRELATION_ID, correlationId, eventId);

            rabbitMqClient.sendProducerMessage(RabbitMQConfig.REQUEST_QUEUE, queueRequest, correlationId);

            log.info(LogConstants.MESSAGE_SENT, RabbitMQConfig.REQUEST_QUEUE, correlationId);
            log.info(LogConstants.END_METHOD, methodName);
            return correlationId;
        } catch (Exception e) {
            log.error(LogConstants.EXCEPTION_OCCURRED, methodName, eventId, "N/A", e.getMessage(), e);
            return null;
        }
    }
}
