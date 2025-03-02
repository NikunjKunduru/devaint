package com.nikunj.devaint.controller;

import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.service.IDevaintService;
import com.nikunj.devaint.service.rabbitmq.consumer.Gpto4oResponseConsumer;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class DevaintController {

    private final IDevaintService devaintService;

    @Autowired
    public DevaintController(IDevaintService devaintService) {
        this.devaintService = devaintService;
    }


    @PostMapping(value = "/triggerRootCauseAnalysis", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> triggerRootCauseAnalysis(@RequestBody String stackTrace) {
        String methodName = "triggerRootCauseAnalysis()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug("Received stackTrace: {}", stackTrace);

        try {
            ResponseEntity<String> rootCauseAnalysis = devaintService.triggerRootCauseAnalysis(stackTrace);
            log.info("Successfully processed root cause analysis.");
            log.info(LogConstants.END_METHOD, methodName);
            return rootCauseAnalysis;
        } catch (Exception e) {
            log.error("Exception in {}: {}", methodName, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing request.");
        }
    }
}
