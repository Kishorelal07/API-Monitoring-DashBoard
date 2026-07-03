package com.apimonitor.controller;

import com.apimonitor.dto.ApiLogRequest;
import com.apimonitor.dto.ApiLogResponse;
import com.apimonitor.dto.ApiStatsResponse;
import com.apimonitor.model.ApiLogStatus;
import com.apimonitor.service.ApiLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiLogController {

    private final ApiLogService apiLogService;

    /** Store an API execution log */
    @PostMapping("/log")
    public ResponseEntity<ApiLogResponse> logApiCall(@Valid @RequestBody ApiLogRequest request) {
        ApiLogResponse response = apiLogService.saveLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns today's logs when no date param is provided.
     * Pass ?date=yyyy-MM-dd to filter by a specific date.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<ApiLogResponse>> getLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) ApiLogStatus status) {

        return ResponseEntity.ok(apiLogService.getLogs(date, apiName, status));
    }

    /** Aggregated stats for today (or the given date) */
    @GetMapping("/stats")
    public ResponseEntity<ApiStatsResponse> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) ApiLogStatus status) {

        return ResponseEntity.ok(apiLogService.getStats(date, apiName, status));
    }
}
