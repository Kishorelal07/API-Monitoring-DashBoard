package com.apimonitor.client.examples;

import com.apimonitor.client.ApiMonitoringClient;

/**
 * Interceptor-style pattern: wrap any outbound HTTP client call.
 * Use this when you have a single HTTP utility used by all API integrations.
 */
public class ApiInterceptorExample {

    private final ApiMonitoringClient monitor;

    public ApiInterceptorExample(ApiMonitoringClient monitor) {
        this.monitor = monitor;
    }

    /**
     * Intercept and monitor any HTTP call made through your shared client.
     */
    public InterceptedResponse interceptAndExecute(String apiName, HttpCallable callable) {
        ApiMonitoringClient.ApiCallContext ctx = monitor.startCall(apiName);
        try {
            InterceptedResponse response = callable.execute();
            if (response.statusCode >= 200 && response.statusCode < 300) {
                ctx.success(response.statusCode);
            } else {
                ctx.failure(response.statusCode, response.errorMessage);
            }
            return response;
        } catch (Exception ex) {
            ctx.failure(ex.getMessage());
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    @FunctionalInterface
    public interface HttpCallable {
        InterceptedResponse execute() throws Exception;
    }

    public static class InterceptedResponse {
        public final int statusCode;
        public final String body;
        public final String errorMessage;

        public InterceptedResponse(int statusCode, String body) {
            this(statusCode, body, null);
        }

        public InterceptedResponse(int statusCode, String body, String errorMessage) {
            this.statusCode = statusCode;
            this.body = body;
            this.errorMessage = errorMessage;
        }
    }
}
