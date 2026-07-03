package com.apimonitor.service;

import com.apimonitor.dto.ApiLogRequest;
import com.apimonitor.dto.ApiLogResponse;
import com.apimonitor.dto.ApiStatsResponse;
import com.apimonitor.model.ApiLog;
import com.apimonitor.model.ApiLogStatus;
import com.apimonitor.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiLogService {

    private final ApiLogRepository apiLogRepository;

    @Transactional
    public ApiLogResponse saveLog(ApiLogRequest request) {
        LocalDateTime responseTimestamp = request.getResponseTimestamp();
        long responseTimeMs = calculateResponseTime(request);

        ApiLog apiLog = ApiLog.builder()
                .apiName(request.getApiName())
                .workItemId(request.getWorkItemId())
                .requestTimestamp(request.getRequestTimestamp())
                .responseTimestamp(responseTimestamp)
                .responseCode(request.getResponseCode())
                .status(request.getStatus())
                .errorMessage(request.getErrorMessage())
                .responseTime(responseTimeMs)
                .build();

        ApiLog saved = apiLogRepository.save(apiLog);
        log.info("Saved API log: apiName={}, workItemId={}, status={}, responseTime={}ms",
                saved.getApiName(), saved.getWorkItemId(), saved.getStatus(), saved.getResponseTime());
        return ApiLogResponse.fromEntity(saved);
    }

    /**
     * Returns today's logs when date is null; otherwise filters by the given date.
     * Optional apiName and status filters are applied in-memory after the date query.
     */
    @Transactional(readOnly = true)
    public List<ApiLogResponse> getLogs(LocalDate date, String apiName, ApiLogStatus status) {
        List<ApiLog> logs = date == null
                ? apiLogRepository.findTodayLogs()
                : apiLogRepository.findByRequestDate(date);

        Stream<ApiLog> stream = logs.stream();
        if (apiName != null && !apiName.isBlank()) {
            stream = stream.filter(log -> apiName.equals(log.getApiName()));
        }
        if (status != null) {
            stream = stream.filter(log -> status == log.getStatus());
        }

        return stream.map(ApiLogResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ApiStatsResponse getStats(LocalDate date, String apiName, ApiLogStatus status) {
        List<ApiLogResponse> logs = getLogs(date, apiName, status);

        long totalCalls = logs.size();
        long successCount = logs.stream()
                .filter(log -> log.getStatus() == ApiLogStatus.SUCCESS)
                .count();
        long failureCount = totalCalls - successCount;
        double avgResponseTime = logs.stream()
                .mapToLong(ApiLogResponse::getResponseTime)
                .average()
                .orElse(0.0);

        Map<String, List<ApiLogResponse>> grouped = logs.stream()
                .collect(Collectors.groupingBy(ApiLogResponse::getApiName));

        List<ApiStatsResponse.ApiNameStats> byApiName = grouped.entrySet().stream()
                .map(entry -> buildApiNameStats(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ApiStatsResponse.ApiNameStats::getApiName))
                .toList();

        return ApiStatsResponse.builder()
                .totalCalls(totalCalls)
                .successCount(successCount)
                .failureCount(failureCount)
                .successRate(calculateRate(successCount, totalCalls))
                .avgResponseTime(round(avgResponseTime))
                .byApiName(byApiName)
                .build();
    }

    private ApiStatsResponse.ApiNameStats buildApiNameStats(String name, List<ApiLogResponse> apiLogs) {
        long apiTotal = apiLogs.size();
        long apiSuccess = apiLogs.stream()
                .filter(log -> log.getStatus() == ApiLogStatus.SUCCESS)
                .count();
        long apiFailure = apiTotal - apiSuccess;
        double apiAvg = apiLogs.stream()
                .mapToLong(ApiLogResponse::getResponseTime)
                .average()
                .orElse(0.0);

        return ApiStatsResponse.ApiNameStats.builder()
                .apiName(name)
                .totalCalls(apiTotal)
                .successCount(apiSuccess)
                .failureCount(apiFailure)
                .successRate(calculateRate(apiSuccess, apiTotal))
                .avgResponseTime(round(apiAvg))
                .build();
    }

    /** Calculates response time in ms from timestamps when not provided by the client */
    private long calculateResponseTime(ApiLogRequest request) {
        if (request.getResponseTime() != null) {
            return request.getResponseTime();
        }
        return Duration.between(request.getRequestTimestamp(), request.getResponseTimestamp()).toMillis();
    }

    private double calculateRate(long success, long total) {
        if (total == 0) {
            return 0.0;
        }
        return round((success * 100.0) / total);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
