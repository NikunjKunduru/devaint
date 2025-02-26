package com.nikunj.devaint.model;

import lombok.Data;

@Data
public class Gpt4oQueueDTO {

    private String eventId;
    private Gpt4oRequestModel requestModel;

    public Gpt4oQueueDTO(String eventId, Gpt4oRequestModel requestModel) {
        this.eventId = eventId;
        this.requestModel = requestModel;
    }
}
