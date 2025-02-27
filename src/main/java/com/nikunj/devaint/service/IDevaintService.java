package com.nikunj.devaint.service;

import com.nikunj.devaint.model.Gpt4oResponseModel;
import org.springframework.http.ResponseEntity;

public interface IDevaintService {

    /**
     * Triggers the root cause analysis of the provided exception stack trace asynchronously.
     * The processing is handled in the background using RabbitMQ, and a response is retrieved later using the correlation ID.
     *
     * @param stackTrace The stack trace of the exception as a String.
     * @return ResponseEntity containing an acknowledgment message with a tracking ID or status.
     */
    ResponseEntity<String> triggerRootCauseAnalysis(String stackTrace);

    /**
     * Retrieves the GPT-4o response using the correlation ID.
     * This method is used to fetch the processed response after asynchronous execution.
     *
     * @param correlationId The unique identifier for tracking the GPT-4o request.
     * @return ResponseEntity containing the GPT-4o response model.
     */
    ResponseEntity<Gpt4oResponseModel> getResponse(String correlationId);
}
