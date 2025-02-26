package com.nikunj.devaint.util;

import com.nikunj.devaint.model.Gpt4oRequestModel;

import java.util.Collections;

public class Gpt4oRequestBodyBuilder {

    private Gpt4oRequestBodyBuilder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Gpt4oRequestModel buildRequestBody(String content) {
        Gpt4oRequestModel.Message message = new Gpt4oRequestModel.Message();
        message.setRole("user");
        message.setContent(content);

        Gpt4oRequestModel requestBody = new Gpt4oRequestModel();
        requestBody.setMessages(Collections.singletonList(message));
        requestBody.setModel("gpt-4o");
        requestBody.setTemperature(1);
        requestBody.setMaxTokens(4096);
        requestBody.setTopP(1);

        return requestBody;
    }
}
