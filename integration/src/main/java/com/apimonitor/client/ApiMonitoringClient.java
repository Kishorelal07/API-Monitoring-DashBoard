package com.apimonitor.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Async logging client — sends API execution details to the Spring Boot dashboard.
 * Runs on a background thread pool so the main API flow is never blocked.
 * Logging failures are swallowed and never affect the caller's response.
 */
public class ApiMonitoringClient {

    private static final Logger LOGGER = Logger.getLogger(ApiMonitoringClient.class.getName());

    private final String monitoringServiceUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;

    public ApiMonitoringClient(String monitoringServiceBaseUrl) {
        this(monitoringServiceBaseUrl, 2);
    }

    public ApiMonitoringClient(String monitoringServiceBaseUrl, int threadPoolSize) {
        this.monitoringServiceUrl = normalizeBaseUrl(monitoringServiceBaseUrl) + "/api/log";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Daemon threads — logging must not keep the JVM alive or block shutdown
        this.executor = Executors.newFixedThreadPool(threadPoolSize, new MonitoringThreadFactory());
    }

    /** Fire-and-forget: submits log to background thread and returns immediately */
    public void logAsync(ApiLogPayload payload) {
        executor.submit(() -> sendLog(payload));
    }

    public void logAsync(String apiName, String workItemId, LocalDateTime requestTimestamp,
                         LocalDateTime responseTimestamp, Integer responseCode,
                         ApiLogStatus status, String errorMessage) {
        logAsync(new ApiLogPayload(apiName, workItemId, requestTimestamp, responseTimestamp,
                responseCode, status, errorMessage, null));
    }

    /** Start tracking an API call — call success() or failure() when done */
    public ApiCallContext startCall(String apiName, String workItemId) {
        return new ApiCallContext(this, apiName, workItemId, LocalDateTime.now());
    }

    public ApiCallContext startCall(String apiName) {
        return startCall(apiName, null);
    }

    private void sendLog(ApiLogPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(monitoringServiceUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                LOGGER.log(Level.WARNING, "Monitoring service returned {0} for apiName={1}",
                        new Object[]{response.statusCode(), payload.getApiName()});
            }
        } catch (Exception e) {
            // Never propagate — logging failure must not affect main API flow
            LOGGER.log(Level.WARNING, "Failed to send API log for " + payload.getApiName() + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("monitoringServiceBaseUrl must not be blank");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private static class MonitoringThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "api-monitor-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }

    /** Tracks one API call lifecycle and sends the log asynchronously on completion */
    public static class ApiCallContext {
        private final ApiMonitoringClient client;
        private final String apiName;
        private final String workItemId;
        private final LocalDateTime requestTimestamp;
        private final long startNanos;

        private ApiCallContext(ApiMonitoringClient client, String apiName, String workItemId,
                               LocalDateTime requestTimestamp) {
            this.client = client;
            this.apiName = apiName;
            this.workItemId = workItemId;
            this.requestTimestamp = requestTimestamp;
            this.startNanos = System.nanoTime();
        }

        public void success(int responseCode) {
            complete(responseCode, ApiLogStatus.SUCCESS, null);
        }

        public void failure(int responseCode, String errorMessage) {
            complete(responseCode, ApiLogStatus.FAILURE, errorMessage);
        }

        public void failure(String errorMessage) {
            complete(null, ApiLogStatus.FAILURE, errorMessage);
        }

        private void complete(Integer responseCode, ApiLogStatus status, String errorMessage) {
            LocalDateTime responseTimestamp = LocalDateTime.now();
            // responseTime is calculated server-side from timestamps; omit here
            client.logAsync(apiName, workItemId, requestTimestamp, responseTimestamp,
                    responseCode, status, errorMessage);
        }
    }
}
