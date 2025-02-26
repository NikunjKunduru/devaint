package com.nikunj.devaint.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Gpt4oResponseModel {

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("created")
    private long created;

    @JsonProperty("id")
    private String id;

    @JsonProperty("model")
    private String model;

    @JsonProperty("object")
    private String object;

    @JsonProperty("prompt_filter_results")
    private List<PromptFilterResult> promptFilterResults;

    @JsonProperty("usage")
    private Usage usage;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {
        @JsonProperty("content_filter_results")
        private ContentFilterResults contentFilterResults;

        @JsonProperty("finish_reason")
        private String finishReason;

        @JsonProperty("index")
        private int index;

        @JsonProperty("logprobs")
        private Object logprobs;

        @JsonProperty("message")
        private Message message;

        @JsonProperty("refusal")
        private Object refusal;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentFilterResults {
        @JsonProperty("hate")
        private FilterResult hate;

        @JsonProperty("protected_material_code")
        private FilterResult protectedMaterialCode;

        @JsonProperty("protected_material_text")
        private FilterResult protectedMaterialText;

        @JsonProperty("self_harm")
        private FilterResult selfHarm;

        @JsonProperty("sexual")
        private FilterResult sexual;

        @JsonProperty("violence")
        private FilterResult violence;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FilterResult {
        @JsonProperty("filtered")
        private boolean filtered;

        @JsonProperty("severity")
        private String severity;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        @JsonProperty("content")
        private String content;

        @JsonProperty("role")
        private String role;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PromptFilterResult {
        @JsonProperty("content_filter_results")
        private ContentFilterResults contentFilterResults;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Usage {
        @JsonProperty("completion_tokens")
        private int completionTokens;

        @JsonProperty("completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;

        @JsonProperty("prompt_tokens")
        private int promptTokens;

        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;

        @JsonProperty("total_tokens")
        private int totalTokens;

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CompletionTokensDetails {
            @JsonProperty("accepted_prediction_tokens")
            private int acceptedPredictionTokens;

            @JsonProperty("audio_tokens")
            private int audioTokens;

            @JsonProperty("reasoning_tokens")
            private int reasoningTokens;

            @JsonProperty("rejected_prediction_tokens")
            private int rejectedPredictionTokens;
        }

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class PromptTokensDetails {
            @JsonProperty("audio_tokens")
            private int audioTokens;

            @JsonProperty("cached_tokens")
            private int cachedTokens;
        }
    }
}