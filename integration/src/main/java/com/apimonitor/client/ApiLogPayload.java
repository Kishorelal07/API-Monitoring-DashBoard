package com.apimonitor.client;

import java.time.LocalDateTime;

/**
 * Payload sent to POST /api/log on the monitoring service.
 */
public class ApiLogPayload {

    private String apiName;
    private String workItemId;
    private LocalDateTime requestTimestamp;
    private LocalDateTime responseTimestamp;
    private Integer responseCode;
    private ApiLogStatus status;
    private String errorMessage;
    private Long responseTime;

    public ApiLogPayload() {
    }

    public ApiLogPayload(String apiName, String workItemId, LocalDateTime requestTimestamp,
                         LocalDateTime responseTimestamp, Integer responseCode, ApiLogStatus status,
                         String errorMessage, Long responseTime) {
        this.apiName = apiName;
        this.workItemId = workItemId;
        this.requestTimestamp = requestTimestamp;
        this.responseTimestamp = responseTimestamp;
        this.responseCode = responseCode;
        this.status = status;
        this.errorMessage = errorMessage;
        this.responseTime = responseTime;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(LocalDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public ApiLogStatus getStatus() {
        return status;
    }

    public void setStatus(ApiLogStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
}
