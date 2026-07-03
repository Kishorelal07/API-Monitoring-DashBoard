package com.apimonitor.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApiStatsResponse {

    private long totalCalls;
    private long successCount;
    private long failureCount;
    private double successRate;
    private double avgResponseTime;
    private List<ApiNameStats> byApiName;

    @Data
    @Builder
    public static class ApiNameStats {
        private String apiName;
        private long totalCalls;
        private long successCount;
        private long failureCount;
        private double successRate;
        private double avgResponseTime;
    }
}
