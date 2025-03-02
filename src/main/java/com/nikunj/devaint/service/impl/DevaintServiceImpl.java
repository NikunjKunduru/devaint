package com.nikunj.devaint.service.impl;

import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.model.Gpt4oRequestModel;
import com.nikunj.devaint.service.IDevaintService;
import com.nikunj.devaint.service.rabbitmq.producer.Gpt4oRequestProducer;
import com.nikunj.devaint.util.Gpt4oRequestBodyBuilder;
import com.nikunj.devaint.util.LogConstants;
import com.nikunj.devaint.util.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class DevaintServiceImpl implements IDevaintService {

    private final Gpt4oRequestProducer producer;

    @Autowired
    public DevaintServiceImpl(Gpt4oRequestProducer producer) {
        this.producer = producer;
    }

    @Override
    public ResponseEntity<String> triggerRootCauseAnalysis(String stackTrace) {
        String methodName = "triggerRootCauseAnalysis()";
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
}
