package com.apimonitor.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_logs", indexes = {
        @Index(name = "idx_api_name", columnList = "api_name"),
        @Index(name = "idx_work_item_id", columnList = "work_item_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_request_timestamp", columnList = "request_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_name", nullable = false, length = 255)
    private String apiName;

    @Column(name = "work_item_id", length = 100)
    private String workItemId;

    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimestamp;

    @Column(name = "response_timestamp", nullable = false)
    private LocalDateTime responseTimestamp;

    @Column(name = "response_code")
    private Integer responseCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApiLogStatus status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "response_time", nullable = false)
    private Long responseTime;
}
