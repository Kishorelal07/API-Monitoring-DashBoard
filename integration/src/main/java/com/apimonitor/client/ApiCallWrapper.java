package com.apimonitor.client;

import java.util.function.Supplier;

/**
 * Wrapper pattern for wrapping any API call with automatic async monitoring.
 */
public final class ApiCallWrapper {

    private final ApiMonitoringClient monitoringClient;

    public ApiCallWrapper(ApiMonitoringClient monitoringClient) {
        this.monitoringClient = monitoringClient;
    }

    public <T> T execute(String apiName, String workItemId, Supplier<T> call,
                         ResponseCodeExtractor<T> responseCodeExtractor) {
        ApiMonitoringClient.ApiCallContext context = monitoringClient.startCall(apiName, workItemId);
        try {
            T result = call.get();
            int code = responseCodeExtractor.extract(result);
            if (code >= 200 && code < 300) {
                context.success(code);
            } else {
                context.failure(code, "Non-success response code: " + code);
            }
            return result;
        } catch (Exception ex) {
            context.failure(ex.getMessage());
            throw ex;
        }
    }

    public <T> T execute(String apiName, Supplier<T> call, ResponseCodeExtractor<T> responseCodeExtractor) {
        return execute(apiName, null, call, responseCodeExtractor);
    }

    public <T> T execute(String apiName, Supplier<T> call) {
        return execute(apiName, null, call, result -> 200);
    }

    @FunctionalInterface
    public interface ResponseCodeExtractor<T> {
        int extract(T result);
    }
}
