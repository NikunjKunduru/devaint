package com.nikunj.devaint.util;

import org.apache.commons.text.StringEscapeUtils;

public class PromptTemplate {

    private PromptTemplate() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String getRootCauseAnalysisPrompt(String stackTrace) {
        String rootCauseAnalysisPrompt =
               """
               I have an exception stack trace that I need to analyze. Please provide a detailed root cause analysis and suggest best practices to fix the issue. Return the response as a JSON object with the following structure:
               {
                 "message": {
                   "Service Affected": {
                     "Class Name": "<Class responsible for the issue>",
                     "Package": "<Package where the class is located>",
                     "Method Name": "<Method causing the issue>",
                     "Line Causing Issue": "<Line number>"
                   },
                   "Root Cause Analysis": "<Detailed explanation of why the exception occurred>",
                   "Best Practices": "<Best practices to prevent the issue in the future>",
                   "Code Changes": "<Code block to resolve the issue>"
                 }
               }
               
               Stack Trace:
               %s
               
               Additional Notes:
               Identify the exact class, method, and line where the issue originated.
               Explain the root cause in simple terms, considering dependencies, input parameters, or incorrect configurations.
               Suggest code fixes and best practices to prevent the issue in the future.
               Ensure accuracy—do not introduce any assumptions, speculations, or AI hallucinations. Base the response strictly on the given stack trace.
               """.formatted(stackTrace);

        return StringEscapeUtils.escapeJava(rootCauseAnalysisPrompt);
    }
}
