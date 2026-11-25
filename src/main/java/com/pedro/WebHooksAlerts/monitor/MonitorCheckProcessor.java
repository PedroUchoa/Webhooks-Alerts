package com.pedro.WebHooksAlerts.monitor;

import com.pedro.WebHooksAlerts.monitor.dtos.MonitorStatusChangedEvent;
import com.pedro.WebHooksAlerts.monitor.dtos.ProbeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorCheckProcessor {

    private final MonitorRepository monitorRepository;
    private final CheckExecutionRepository checkExecutionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.monitor.failure-threshold:2}")
    private int failureThreshold;

    @Transactional
    public void recordResult(UUID monitorId, ProbeResult result){
        Monitor monitor = monitorRepository.findById(monitorId).orElse(null);
        if (monitor == null) return;

        CheckExecution execution = new CheckExecution(monitor.getId(),result.HttpStatus(), result.latencyMs(), result.success(), result.errorMessage());
        checkExecutionRepository.save(execution);

        monitor.applyCheckResult(result.success(), failureThreshold)
                .ifPresent(newStatus->{
                    log.info("Status Transition Detected: monitor={} url={} newStatus={}", monitor.getName(),monitor.getTargetUrl(),newStatus);
                    eventPublisher.publishEvent(new MonitorStatusChangedEvent(monitor.getId(),monitor.getName(),monitor.getTargetUrl(),newStatus,result.HttpStatus(),result.latencyMs(),result.errorMessage(), Instant.now()));
                });

        monitorRepository.save(monitor);

    }

}
