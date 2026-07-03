package com.apimonitor.dto;

import com.apimonitor.model.ApiLog;
import com.apimonitor.model.ApiLogStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiLogResponse {

    private Long id;
    private String apiName;
    private String workItemId;
    private LocalDateTime requestTimestamp;
    private LocalDateTime responseTimestamp;
    private Integer responseCode;
    private ApiLogStatus status;
    private String errorMessage;
    private Long responseTime;

    public static ApiLogResponse fromEntity(ApiLog log) {
        return ApiLogResponse.builder()
                .id(log.getId())
                .apiName(log.getApiName())
                .workItemId(log.getWorkItemId())
                .requestTimestamp(log.getRequestTimestamp())
                .responseTimestamp(log.getResponseTimestamp())
                .responseCode(log.getResponseCode())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .responseTime(log.getResponseTime())
                .build();
    }
}
