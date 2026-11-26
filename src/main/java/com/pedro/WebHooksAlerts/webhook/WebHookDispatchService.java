package com.pedro.WebHooksAlerts.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedro.WebHooksAlerts.monitor.MonitorStatus;
import com.pedro.WebHooksAlerts.monitor.dtos.MonitorStatusChangedEvent;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookPayload;
import com.pedro.WebHooksAlerts.webhook.enums.WebhookEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebHookDispatchService {

    private final WebHookSubscriptionRepository subscriptionRepository;
    private final WebHookDeliveryRepository deliveryRepository;
    private ObjectMapper objectMapper;

    @Transactional
    public List<UUID> createPendingDeliveries(MonitorStatusChangedEvent event){
        WebhookEventType eventType = mapEventType(event.newStatus());
        if (eventType == null) return List.of();



        List<WebHookSubscription> subscriptions = subscriptionRepository
                .findByMonitorIdAndActiveTrue(event.monitorId())
                .stream()
                .filter(sub -> sub.getEvents().contains(eventType))
                .toList();

        if (subscriptions.isEmpty()) return List.of();

        String payloadJson = serializePayload(event,eventType);

        List<UUID> createdIds = new ArrayList<>();
        for (WebHookSubscription subscription:subscriptions){
            WebHookDelivery delivery = new WebHookDelivery(subscription,eventType,payloadJson);
            createdIds.add(delivery.getId());
        }
        return createdIds;
    }

    private String serializePayload(MonitorStatusChangedEvent event, WebhookEventType eventType) {
        WebHookPayload payload = new WebHookPayload(
                UUID.randomUUID(),
                eventType,
                event.monitorId(),
                event.monitorName(),
                event.targetUrl(),
                event.httpStatus(),
                event.latencyMs(),
                event.errorMessage(),
                event.occurredAt()
        );
        try{
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed Trying To Serialize The Webhook Payload");
        }

    }

    private WebhookEventType mapEventType(MonitorStatus status) {
        return switch (status){
            case DOWN -> WebhookEventType.MONITOR_DOWN;
            case UP -> WebhookEventType.MONITOR_RECOVERED;
            case PENDING -> null;
        };
    }


}
