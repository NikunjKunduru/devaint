package com.nikunj.devaint.service.rabbitmq.consumer;

import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class Gpto4oResponseConsumer {

    private final ConcurrentHashMap<String, Gpt4oResponseModel> responseStore = new ConcurrentHashMap<>();

    @RabbitListener(queues = "gpt4o_response_queue", concurrency = "3-10")
    public void storeResponse(Gpt4oResponseModel response, Message message) {
        String methodName = "storeResponse()";
        log.info(LogConstants.START_METHOD, methodName);

        String correlationId = message.getMessageProperties().getCorrelationId();
        log.debug(LogConstants.PROCESSING_EVENT, "N/A", correlationId);

        if (correlationId != null) {
            responseStore.put(correlationId, response);
            log.info(LogConstants.RESPONSE_STORED, correlationId);
        } else {
            log.warn(LogConstants.RESPONSE_NOT_FOUND, correlationId);
        }

        log.info(LogConstants.END_METHOD, methodName);
    }

    public ResponseEntity<Gpt4oResponseModel> getResponse(String correlationId) {
        String methodName = "getResponse()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug(LogConstants.PROCESSING_EVENT, "N/A", correlationId);

        try {
            Gpt4oResponseModel response = responseStore.getOrDefault(correlationId, null);

            if (response != null) {
                log.info(LogConstants.RESPONSE_STORED, correlationId);
                log.info(LogConstants.END_METHOD, methodName);
                return ResponseEntity.ok(response);
            } else {
                log.warn(LogConstants.RESPONSE_NOT_FOUND, correlationId);
                log.info(LogConstants.END_METHOD, methodName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(LogConstants.EXCEPTION_OCCURRED, methodName, "N/A", correlationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
