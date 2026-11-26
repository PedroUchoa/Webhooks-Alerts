package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.monitor.dtos.MonitorStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebHookEventListener {

    private final WebHookDispatchService dispatchService;
    private final WebHookQueuePublisher queuePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMonitorStatusChanged(MonitorStatusChangedEvent event){
        List<UUID> deliveryIds = dispatchService.createPendingDeliveries(event);

        for (UUID deliveryId : deliveryIds){
            queuePublisher.publishForDelivery(deliveryId);
        }

        if (!deliveryIds.isEmpty()){
            log.info(
                    "Queueing {} Webhooks To Monitors {} (status={})",
                    deliveryIds.size(), event.monitorId(), event.newStatus()
            );
        }

    }

}
