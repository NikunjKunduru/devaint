package com.nikunj.devaint.client;

import com.nikunj.devaint.model.Gpt4oRequestModel;
import com.nikunj.devaint.model.Gpt4oResponseModel;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class Gpt4oClient {

    private final RestTemplate restTemplate;

    @Autowired
    public Gpt4oClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Gpt4oResponseModel> getRootCauseAnalysis(String gpt4oUrl, Gpt4oRequestModel requestBody, HttpHeaders headers) {
        String methodName = "getRootCauseAnalysis()";
        log.info(LogConstants.START_METHOD, methodName);
        log.debug(LogConstants.API_CALL, gpt4oUrl);

        HttpEntity<Gpt4oRequestModel> request = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Sending request to GPT-4o API: {}", requestBody);
            ResponseEntity<Gpt4oResponseModel> response = restTemplate.exchange(gpt4oUrl, HttpMethod.POST, request, Gpt4oResponseModel.class);
            log.info(LogConstants.API_CALL_SUCCESS);
            log.debug(LogConstants.API_RESPONSE, response.getBody());
            log.info(LogConstants.END_METHOD, methodName);
            return response;
        } catch (HttpStatusCodeException e) {
            log.error(LogConstants.API_CALL_FAILURE, e.getStatusCode(), e.getResponseBodyAsString());
            log.info(LogConstants.END_METHOD, methodName);
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error(LogConstants.UNEXPECTED_ERROR, e);
            log.info(LogConstants.END_METHOD, methodName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
