package com.nikunj.devaint.util;

public class LogConstants {

    private LogConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String START_METHOD = "Start Method: {}";
    public static final String END_METHOD = "End Method: {}";
    public static final String PROCESSING_EVENT = "Processing Event ID: {} | Correlation ID: {}";
    public static final String GENERATED_CORRELATION_ID = "Generated Correlation ID: {} for Event ID: {}";
    public static final String MESSAGE_SENT = "Message sent to queue: {} with Correlation ID: {}";
    public static final String RESPONSE_STORED = "Response stored successfully for Correlation ID: {}";
    public static final String RESPONSE_NOT_FOUND = "No response found for Correlation ID: {}";
    public static final String EXCEPTION_OCCURRED = "Exception in {} for Event ID: {} | Correlation ID: {} | Error: {}";
    public static final String RETRY_ATTEMPT = "Retry attempt {} for Correlation ID: {}";
    public static final String MAX_RETRIES_REACHED = "Max retries reached for Correlation ID: {} | Moving message to DLQ";
    public static final String API_CALL = "Calling GPT-4o API at URL: {}";
    public static final String API_CALL_SUCCESS = "Successfully received response from GPT-4o API for Event ID: {} | Correlation ID: {}";
    public static final String API_RESPONSE = "GPT-4o API Response: {}";
    public static final String UNEXPECTED_ERROR = "Unexpected error while calling GPT-4o API: {}";
    public static final String API_CALL_FAILURE = "GPT-4o API call failed: Status = {}, Response = {}";
    public static final String EMPTY_RESPONSE = "Empty response from GPT-4o API for Event ID: {} | Correlation ID: {}";


}
