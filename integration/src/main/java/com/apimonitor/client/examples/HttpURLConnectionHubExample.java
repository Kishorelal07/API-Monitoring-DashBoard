package com.apimonitor.client.examples;

import com.apimonitor.client.HttpURLConnectionApiLogger;

/**
 * Example: integrate monitoring into your existing central API hub
 * where all outbound calls use HttpURLConnection.
 *
 * Steps to integrate:
 * 1. Add api-monitoring-client JAR to your WAR/JAR classpath
 * 2. Initialize HttpURLConnectionApiLogger once at startup
 * 3. Route all external API calls through callExternalApi()
 */
public class HttpURLConnectionHubExample {

    // Initialize once — e.g. in ServletContextListener or @PostConstruct
    private static final HttpURLConnectionApiLogger API_LOGGER =
            new HttpURLConnectionApiLogger(
                    System.getProperty("api.monitor.url", "http://localhost:8080"));

    /**
     * Your existing central hub method — BEFORE (no monitoring):
     */
    public String callExternalApiLegacy(String apiName, String workItemId, String url, String payload) {
        // ... raw HttpURLConnection code ...
        return "{}";
    }

    /**
     * AFTER — single line change routes through monitored method:
     */
    public String callExternalApi(String apiName, String workItemId, String url, String payload) {
        return API_LOGGER.callExternalApi(apiName, workItemId, url, "POST", payload);
    }

    // Example usage from your business layer
    public String fetchAccountBalance(String workItemId, String accountNo) {
        String url = "https://cbs-api.example.com/account/balance";
        String payload = "{\"accountNo\":\"" + accountNo + "\"}";
        return callExternalApi("CBS_ACCOUNT_BALANCE", workItemId, url, payload);
    }

    public String initiatePayment(String workItemId, String paymentJson) {
        String url = "https://fintech-api.example.com/payment";
        return callExternalApi("FINTECH_PAYMENT", workItemId, url, paymentJson);
    }
}
