package com.nikunj.devaint.controller;

import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.service.IDevaintService;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class DevaintController {

    @Autowired
    private IDevaintService devaintService;

    @PostMapping(value = "/getRootCause", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> getRootCause(@RequestBody String stackTrace) {
        String methodName = "getRootCause()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug("Received stackTrace: {}", stackTrace);

        try {
            ResponseEntity<String> rootCauseAnalysis = devaintService.getRootCause(stackTrace);
            log.info("Successfully processed root cause analysis.");
            log.info(LogConstants.END_METHOD, methodName);
            return rootCauseAnalysis;
        } catch (Exception e) {
            log.error("Exception in {}: {}", methodName, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing request.");
        }
    }

    @GetMapping(value = "/response/{correlationId}", produces = "application/json")
    public ResponseEntity<Gpt4oResponseModel> getResponse(@PathVariable String correlationId) {
        String methodName = "getResponse()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug("Fetching response for correlationId: {}", correlationId);

        try {
            ResponseEntity<Gpt4oResponseModel> response = devaintService.getResponse(correlationId);
            if (response.getBody() != null) {
                log.info("Successfully retrieved response for correlationId: {}", correlationId);
            } else {
                log.warn("No response found for correlationId: {}", correlationId);
            }
            log.info(LogConstants.END_METHOD, methodName);
            return response;
        } catch (Exception e) {
            log.error("Exception in {} for correlationId {}: {}", methodName, correlationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
