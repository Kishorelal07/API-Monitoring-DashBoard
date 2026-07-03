package com.apimonitor.dto;

import com.apimonitor.model.ApiLogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiLogRequest {

    @NotBlank(message = "apiName is required")
    private String apiName;

    private String workItemId;

    @NotNull(message = "requestTimestamp is required")
    private LocalDateTime requestTimestamp;

    @NotNull(message = "responseTimestamp is required")
    private LocalDateTime responseTimestamp;

    private Integer responseCode;

    @NotNull(message = "status is required")
    private ApiLogStatus status;

    private String errorMessage;

    /** Optional — server calculates from timestamps when omitted */
    private Long responseTime;
}
