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
        log.debug("Processing request for Event ID: {} with Correlation ID: {}", queueRequest.getEventId(), correlationId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + gpt4oApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            log.debug("Sending request to GPT-4o API at {}", gpt4oApiUrl);
            ResponseEntity<Gpt4oResponseModel> responseEntity = gpt4oClient.getRootCauseAnalysis(gpt4oApiUrl, queueRequest.getRequestModel(), headers);
            Gpt4oResponseModel response = responseEntity.getBody();

            if (response != null) {
                log.info("Successfully received response from GPT-4o API for Correlation ID: {}", correlationId);
                rabbitTemplate.convertAndSend(RabbitMQConfig.RESPONSE_QUEUE, response, msg -> {
                    msg.getMessageProperties().setCorrelationId(correlationId);
                    return msg;
                });
                log.info("Sent response to queue: {} for Correlation ID: {}", RabbitMQConfig.RESPONSE_QUEUE, correlationId);
            } else {
                log.error("Failed to process request for Event ID: {} - Empty response from GPT-4o API", queueRequest.getEventId());
            }
        } catch (Exception e) {
            log.error("Exception in {} for Event ID {}: {}", methodName, queueRequest.getEventId(), e.getMessage(), e);
        }

        log.info(LogConstants.END_METHOD, methodName);
    }
}
