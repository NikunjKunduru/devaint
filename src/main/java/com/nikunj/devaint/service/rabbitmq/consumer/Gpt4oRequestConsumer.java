package com.nikunj.devaint.service.rabbitmq.consumer;

import com.nikunj.devaint.client.Gpt4oClient;
import com.nikunj.devaint.config.RabbitMQConfig;
import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.client.RabbitMqClient;
import com.nikunj.devaint.service.rabbitmq.RetryService;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
    private final RabbitMqClient rabbitMqClient;
    private final RetryService retryService;

    @Autowired
    public Gpt4oRequestConsumer(Gpt4oClient gpt4oClient, RabbitMqClient rabbitMqClient, RetryService retryService) {
        this.gpt4oClient = gpt4oClient;
        this.rabbitMqClient = rabbitMqClient;
        this.retryService = retryService;
    }

    @RabbitListener(queues = RabbitMQConfig.REQUEST_QUEUE, concurrency = "3-10")
    public void processMessage(Gpt4oQueueDTO queueRequest, Message message) {
        String methodName = "processMessage()";
        log.info(LogConstants.START_METHOD, methodName);

        String correlationId = message.getMessageProperties().getCorrelationId();
        log.debug(LogConstants.PROCESSING_EVENT, queueRequest.getEventId(), correlationId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + gpt4oApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            log.debug(LogConstants.API_CALL, gpt4oApiUrl);
            ResponseEntity<Gpt4oResponseModel> responseEntity = gpt4oClient.getRootCauseAnalysis(gpt4oApiUrl, queueRequest.getRequestModel(), headers);

            if (responseEntity.getBody() != null) {
                log.info(LogConstants.API_CALL_SUCCESS, queueRequest.getEventId(), correlationId);
                rabbitMqClient.sendConsumerMessage(RabbitMQConfig.RESPONSE_QUEUE, responseEntity.getBody(), correlationId);
                log.info(LogConstants.MESSAGE_SENT, RabbitMQConfig.RESPONSE_QUEUE, correlationId);
            } else {
                log.error(LogConstants.EMPTY_RESPONSE, queueRequest.getEventId(), correlationId);
            }
        } catch (Exception e) {
            log.error(LogConstants.EXCEPTION_OCCURRED, methodName, queueRequest.getEventId(), correlationId, e.getMessage(), e);
            retryService.retryMessage(queueRequest, message);
        }

        log.info(LogConstants.END_METHOD, methodName);
    }
}
