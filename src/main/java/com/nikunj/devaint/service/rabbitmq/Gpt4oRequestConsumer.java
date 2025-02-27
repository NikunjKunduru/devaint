package com.nikunj.devaint.service.rabbitmq;

import com.nikunj.devaint.client.Gpt4oClient;
import com.nikunj.devaint.config.RabbitMQConfig;
import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Gpt4oRequestConsumer {

    @Value("${gpt4o.api-url}")
    private String gpt4oApiUrl;

    @Value("${gpt4o.api-key}")
    private String gpt4oApiKey;

    private final Gpt4oClient gpt4oClient;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public Gpt4oRequestConsumer(Gpt4oClient gpt4oClient, RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
        this.gpt4oClient = gpt4oClient;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    @RabbitListener(queues = RabbitMQConfig.REQUEST_QUEUE, concurrency = "3-10")
    public void processMessage(Gpt4oQueueDTO queueRequest, Message message) {
        String methodName = "processMessage()";
        log.info(LogConstants.START_METHOD, methodName);

        String correlationId = message.getMessageProperties().getCorrelationId();
        log.debug("Processing Event ID: {} | Correlation ID: {}", queueRequest.getEventId(), correlationId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + gpt4oApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            log.debug("Sending request to GPT-4o API for Event ID: {} | Correlation ID: {}", queueRequest.getEventId(), correlationId);
            ResponseEntity<Gpt4oResponseModel> responseEntity = gpt4oClient.getRootCauseAnalysis(gpt4oApiUrl, queueRequest.getRequestModel(), headers);

            if (responseEntity.getBody() != null) {
                log.info("Successfully received response from GPT-4o API for Event ID: {} | Correlation ID: {}", queueRequest.getEventId(), correlationId);
                rabbitTemplate.convertAndSend(RabbitMQConfig.RESPONSE_QUEUE, responseEntity.getBody(), msg -> {
                    msg.getMessageProperties().setCorrelationId(correlationId);
                    return msg;
                });
                log.info("Sent response to RESPONSE_QUEUE for Event ID: {} | Correlation ID: {}", queueRequest.getEventId(), correlationId);
            } else {
                log.error("Empty response from GPT-4o API for Event ID: {} | Correlation ID: {}", queueRequest.getEventId(), correlationId);
            }

        } catch (Exception e) {
            log.error("Exception in {} for Event ID: {} | Correlation ID: {} | Error: {}", methodName, queueRequest.getEventId(), correlationId, e.getMessage(), e);
            retryMessage(queueRequest, message);
        }

        log.info(LogConstants.END_METHOD, methodName);
    }

    private void retryMessage(Gpt4oQueueDTO queueRequest, Message message) {
        String methodName = "retryMessage()";
        log.info(LogConstants.START_METHOD, methodName);

        Integer retryCount = message.getMessageProperties().getHeader("x-retry-count");
        retryCount = (retryCount == null) ? 1 : retryCount + 1;

        log.warn("Retry attempt {} for Event ID: {} | Correlation ID: {}", retryCount, queueRequest.getEventId(), message.getMessageProperties().getCorrelationId());

        String retryQueue;
        if (retryCount == 1) {
            retryQueue = RabbitMQConfig.RETRY_QUEUE_1;
        } else if (retryCount == 2) {
            retryQueue = RabbitMQConfig.RETRY_QUEUE_2;
        } else if (retryCount == 3) {
            retryQueue = RabbitMQConfig.RETRY_QUEUE_3;
        } else {
            log.error("Max retries reached for Event ID: {} | Correlation ID: {} | Moving message to DLQ", queueRequest.getEventId(), message.getMessageProperties().getCorrelationId());
            retryQueue = RabbitMQConfig.DLQ;
        }

        final Integer finalRetryCount = retryCount;
        log.info("Sending Event ID: {} | Correlation ID: {} to queue: {}", queueRequest.getEventId(), message.getMessageProperties().getCorrelationId(), retryQueue);

        rabbitTemplate.convertAndSend(retryQueue, queueRequest, msg -> {
            msg.getMessageProperties().setHeader("x-retry-count", finalRetryCount);
            return msg;
        });

        log.info(LogConstants.END_METHOD, methodName);
    }
}
