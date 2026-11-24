package com.pedro.WebHooksAlerts.monitor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_executions")
@Getter
@NoArgsConstructor
public class CheckExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "latency_ms", nullable = false)
    private Integer latencyMS;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "checked_at", nullable = false, updatable = false)
    private Instant checkedAt;

    public CheckExecution(
            UUID monitorId,
            Integer httpStatus,
            Integer latencyMs,
            boolean success,
            String errorMessage
    ) {
        this.monitorId = monitorId;
        this.httpStatus = httpStatus;
        this.latencyMS = latencyMs;
        this.success = success;
        this.errorMessage = errorMessage;
        this.checkedAt = Instant.now();
    }

}
