package com.pedro.WebHooksAlerts.monitor;

import com.pedro.WebHooksAlerts.monitor.dtos.MonitorTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorClaimService {

    private final MonitorRepository monitorRepository;

    @Transactional
    public List<MonitorTarget> claimDueMonitors(int batchSize){
        Instant now = Instant.now();
        List<Monitor> dueMonitors = monitorRepository.findDueMonitorsForUpdate(now,batchSize);

        for (Monitor monitor: dueMonitors){
            monitor.scheduleNextCheck(now);
        }

        monitorRepository.saveAll(dueMonitors);
        return dueMonitors.stream().map(m->new MonitorTarget(m.getId(), m.getName(), m.getTargetUrl(),m.getTimeoutMs()))
                .toList();

    }

}
