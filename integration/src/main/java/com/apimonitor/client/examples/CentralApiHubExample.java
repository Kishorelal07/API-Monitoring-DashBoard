package com.apimonitor.client.examples;

import com.apimonitor.client.ApiCallWrapper;
import com.apimonitor.client.ApiMonitoringClient;

/**
 * Example showing how to plug monitoring into your existing central API hub method.
 *
 * Copy the pattern into your JAR's common API invocation service.
 */
public class CentralApiHubExample {

    // Initialize once at application startup (e.g. in a servlet context listener or main)
    private static final ApiMonitoringClient MONITOR =
            new ApiMonitoringClient(System.getProperty("api.monitor.url", "http://localhost:8080"));

    private static final ApiCallWrapper WRAPPER = new ApiCallWrapper(MONITOR);

    /**
     * BEFORE: Your existing central method that routes to Fintech/CBS APIs.
     */
    public String callExternalApiLegacy(String apiName, String requestPayload) {
        // ... existing HTTP call logic ...
        return "{\"status\":\"ok\"}";
    }

    /**
     * AFTER: Wrap the central method with monitoring — minimal change to existing code.
     */
    public String callExternalApi(String apiName, String requestPayload) {
        return WRAPPER.execute(apiName, () -> invokeExternalHttp(apiName, requestPayload),
                response -> extractHttpStatus(response));
    }

    /**
     * ALTERNATIVE: Manual context pattern for fine-grained control.
     */
    public String callExternalApiManual(String apiName, String requestPayload) {
        ApiMonitoringClient.ApiCallContext ctx = MONITOR.startCall(apiName);
        try {
            HttpResult result = invokeExternalHttpWithStatus(apiName, requestPayload);
            if (result.statusCode >= 200 && result.statusCode < 300) {
                ctx.success(result.statusCode);
            } else {
                ctx.failure(result.statusCode, result.body);
            }
            return result.body;
        } catch (Exception ex) {
            ctx.failure(ex.getMessage());
            throw ex;
        }
    }

    // --- Simulated external API calls (replace with your real implementation) ---

    private String invokeExternalHttp(String apiName, String requestPayload) {
        if ("CBS_ACCOUNT_BALANCE".equals(apiName)) {
            return "{\"balance\":15000}";
        }
        if ("FINTECH_PAYMENT".equals(apiName)) {
            throw new RuntimeException("Payment gateway timeout");
        }
        return "{\"status\":\"ok\"}";
    }

    private HttpResult invokeExternalHttpWithStatus(String apiName, String requestPayload) {
        if ("FINTECH_PAYMENT".equals(apiName)) {
            return new HttpResult(504, "Gateway Timeout");
        }
        return new HttpResult(200, invokeExternalHttp(apiName, requestPayload));
    }

    private int extractHttpStatus(String response) {
        return 200;
    }

    private static class HttpResult {
        final int statusCode;
        final String body;

        HttpResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    public static void main(String[] args) {
        CentralApiHubExample hub = new CentralApiHubExample();

        System.out.println("Calling CBS API...");
        System.out.println(hub.callExternalApi("CBS_ACCOUNT_BALANCE", "{}"));

        System.out.println("Calling Fintech API (will fail)...");
        try {
            hub.callExternalApi("FINTECH_PAYMENT", "{}");
        } catch (RuntimeException ignored) {
            System.out.println("Expected failure logged asynchronously.");
        }

        MONITOR.shutdown();
    }
}
