package com.nikunj.devaint.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Gpt4oRequestModel {
    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("model")
    private String model;

    @JsonProperty("temperature")
    private int temperature;

    @JsonProperty("max_tokens")
    private int maxTokens;

    @JsonProperty("top_p")
    private int topP;

    // Getters and setters
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;

        // Getters and setters
    }
}