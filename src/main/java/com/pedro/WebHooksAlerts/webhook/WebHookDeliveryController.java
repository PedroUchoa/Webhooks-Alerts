package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.monitor.MonitorService;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookDeliveryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Webhook Deliveries", description = "Delivery auditing and manual DLQ redrive")
public class WebHookDeliveryController {

    private final WebHookDeliveryRepository deliveryRepository;
    private final WebHookDeliveryProcessor processor;
    private final WebHookQueuePublisher queuePublisher;
    private final MonitorService monitorService;

    @GetMapping("/api/monitors/{monitorId}/deliveries")
    @Operation(summary = "Lists the last 50 delivereds webhook from one monitor")
    public ResponseEntity<List<WebHookDeliveryResponse>> listByMonitor(@PathVariable UUID monitorId){
        monitorService.ensureExists(monitorId);
        List<WebHookDeliveryResponse> deliveries = deliveryRepository
                .findTop50BySubscriptionMonitorIdOrderByCreatedAtDesc(monitorId)
                .stream()
                .map(WebHookDeliveryResponse::from).toList();
        return ResponseEntity.ok(deliveries);
    }

    @PostMapping("/api/deliveries/{deliveryId}/retry")
    @Operation(summary = "Restacks manually one deliverey with FAILED Status")
    public ResponseEntity<WebHookDeliveryResponse> manualRetry(@PathVariable UUID deliveryId){
        WebHookDeliveryResponse updated = processor.prepareManualRetry(deliveryId);
        queuePublisher.publishForDelivery(deliveryId);
        return ResponseEntity.accepted().body(updated);
    }

}
