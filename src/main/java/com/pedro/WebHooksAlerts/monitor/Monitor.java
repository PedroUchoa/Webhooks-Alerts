package com.pedro.WebHooksAlerts.monitor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitors")
@Getter
@Setter
@NoArgsConstructor
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(name = "interval_seconds", nullable = false)
    private Integer intervalSeconds;

    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_status", nullable = false, length = 20)
    private MonitorStatus lastStatus = MonitorStatus.PENDING;

    @Column(name = "consecutive_failures", nullable = false)
    private Integer consecutiveFailures = 0;

    @Column(name = "next_check_at", nullable = false)
    private Instant nextCheckAt;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created+at", nullable = false, updatable = false)
    private Instant createdAt;

    public Monitor(String name, String targetUrl, Integer intervalSeconds, Integer timeoutMs) {
        this.name = name;
        this.targetUrl = targetUrl;
        this.intervalSeconds = intervalSeconds;
        this.timeoutMs = timeoutMs != null ? timeoutMs : 5000;
        this.nextCheckAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public void pause() {
        this.active = false;
    }

    public void resume() {
        this.active = true;
        this.nextCheckAt = Instant.now();
    }

}
