package com.pedro.WebHooksAlerts.monitor;

import com.pedro.WebHooksAlerts.monitor.dtos.MonitorTarget;
import com.pedro.WebHooksAlerts.monitor.dtos.ProbeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorCheckScheduler {

    private final MonitorClaimService claimService;
    private final HttpProbeClient probeClient;
    private final MonitorCheckProcessor checkProcessor;

    @Value("${app.monitor.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.monitor.fixed-delay-ms:10000}")
    public void runChecks(){
        List<MonitorTarget> dueMonitors = claimService.claimDueMonitors(batchSize);
        if (dueMonitors.isEmpty())return;

        log.debug("Initializing Parallel Verification of {} Monitor(s) With Virtual Threads", dueMonitors.size());

        try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
            for (MonitorTarget target:dueMonitors){
                executor.submit(()->executeSingleCheck(target));
            }
        }

    }

    private void executeSingleCheck(MonitorTarget target) {
        try {
            ProbeResult result = probeClient.check(target.targetUrl(), target.timeoutMs());
            checkProcessor.recordResult(target.id(),result);
        }catch (Exception ex){
            log.error("Error to Process The Monitor Verification {}", target.id(),ex);

        }
    }

}
