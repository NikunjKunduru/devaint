package com.nikunj.devaint.service.impl;

import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.model.Gpt4oRequestModel;
import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.service.IDevaintService;
import com.nikunj.devaint.service.rabbitmq.Gpt4oRequestProducer;
import com.nikunj.devaint.util.Gpt4oRequestBodyBuilder;
import com.nikunj.devaint.util.LogConstants;
import com.nikunj.devaint.util.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DevaintServiceImpl implements IDevaintService {

    private final Gpt4oRequestProducer producer;

    /**
     * ConcurrentHashMap to store GPT-4o responses with correlation IDs.
     * Ensures thread-safe storage and retrieval of responses.
     */
    private final ConcurrentHashMap<String, Gpt4oResponseModel> responseStore = new ConcurrentHashMap<>();

    public DevaintServiceImpl(Gpt4oRequestProducer producer) {
        this.producer = producer;
    }

    @Override
    public ResponseEntity<String> getRootCause(String stackTrace) {
        String methodName = "getRootCause()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug("Received stack trace for analysis");

        try {
            String eventId = UUID.randomUUID().toString();
            log.debug("Generated event ID: {}", eventId);

            Gpt4oRequestModel requestBody = Gpt4oRequestBodyBuilder.buildRequestBody(
                    PromptTemplate.getRootCauseAnalysisPrompt(stackTrace)
            );

            String correlationId = producer.sendRequestToQueue(eventId, new Gpt4oQueueDTO(eventId, requestBody));
            log.info("Request sent to RabbitMQ with Correlation ID: {}", correlationId);
            log.info(LogConstants.END_METHOD, methodName);

            return ResponseEntity.ok("Request received. Track it with ID: " + correlationId);
        } catch (Exception e) {
            log.error("Exception in {}: {}", methodName, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to process request");
        }
    }

    @RabbitListener(queues = "gpt4o_response_queue")
    public void storeResponse(Gpt4oResponseModel response, org.springframework.amqp.core.Message message) {
        String methodName = "storeResponse()";
        log.info(LogConstants.START_METHOD, methodName);

        String correlationId = message.getMessageProperties().getCorrelationId();
        log.debug("Received response for Correlation ID: {}", correlationId);

        if (correlationId != null) {
            responseStore.put(correlationId, response);
            log.info("Response stored successfully for Correlation ID: {}", correlationId);
        } else {
            log.warn("Received response without a correlation ID. Discarding response.");
        }

        log.info(LogConstants.END_METHOD, methodName);
    }

    @Override
    public ResponseEntity<Gpt4oResponseModel> getResponse(String correlationId) {
        String methodName = "getResponse()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug("Fetching response for Correlation ID: {}", correlationId);

        try {
            Gpt4oResponseModel response = responseStore.getOrDefault(correlationId, null);

            if (response != null) {
                log.info("Successfully retrieved response for Correlation ID: {}", correlationId);
                log.info(LogConstants.END_METHOD, methodName);
                return ResponseEntity.ok(response);
            } else {
                log.warn("No response found for Correlation ID: {}", correlationId);
                log.info(LogConstants.END_METHOD, methodName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Exception in {} for Correlation ID {}: {}", methodName, correlationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
