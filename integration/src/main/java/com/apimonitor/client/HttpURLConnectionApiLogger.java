package com.apimonitor.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Wraps HttpURLConnection calls with async monitoring.
 * Plug this into your existing central API hub method.
 */
public class HttpURLConnectionApiLogger {

    private final ApiMonitoringClient monitoringClient;

    public HttpURLConnectionApiLogger(String monitoringServiceBaseUrl) {
        this.monitoringClient = new ApiMonitoringClient(monitoringServiceBaseUrl);
    }

    public HttpURLConnectionApiLogger(ApiMonitoringClient monitoringClient) {
        this.monitoringClient = monitoringClient;
    }

    /**
     * Central method pattern — all external API calls go through here.
     *
     * @param apiName    logical name e.g. "CBS_ACCOUNT_BALANCE"
     * @param workItemId business/work item reference
     * @param targetUrl  full URL of the external API
     * @param method     GET, POST, PUT, etc.
     * @param body       request body (null for GET)
     * @return response body as String
     */
    public String callExternalApi(String apiName, String workItemId, String targetUrl,
                                  String method, String body) {
        LocalDateTime requestTimestamp = LocalDateTime.now();
        HttpURLConnection connection = null;

        try {
            connection = openConnection(targetUrl, method, body);
            int responseCode = connection.getResponseCode();
            String responseBody = readResponse(connection, responseCode);
            LocalDateTime responseTimestamp = LocalDateTime.now();

            ApiLogStatus status = (responseCode >= 200 && responseCode < 300)
                    ? ApiLogStatus.SUCCESS
                    : ApiLogStatus.FAILURE;
            String errorMessage = status == ApiLogStatus.FAILURE ? responseBody : null;

            // Async log — does not block return of the API response
            sendLogAsync(apiName, workItemId, requestTimestamp, responseTimestamp,
                    responseCode, status, errorMessage);

            if (status == ApiLogStatus.FAILURE) {
                throw new RuntimeException("API call failed with code " + responseCode + ": " + responseBody);
            }
            return responseBody;

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            LocalDateTime responseTimestamp = LocalDateTime.now();
            sendLogAsync(apiName, workItemId, requestTimestamp, responseTimestamp,
                    null, ApiLogStatus.FAILURE, ex.getMessage());
            throw new RuntimeException("API call failed: " + ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(String targetUrl, String method, String body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(targetUrl).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(60_000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        return conn;
    }

    private String readResponse(HttpURLConnection conn, int responseCode) throws Exception {
        InputStream stream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    /** Submits log on a background thread — failures are ignored */
    private void sendLogAsync(String apiName, String workItemId,
                              LocalDateTime requestTimestamp, LocalDateTime responseTimestamp,
                              Integer responseCode, ApiLogStatus status, String errorMessage) {
        monitoringClient.logAsync(apiName, workItemId, requestTimestamp, responseTimestamp,
                responseCode, status, errorMessage);
    }

    public void shutdown() {
        monitoringClient.shutdown();
    }
}
