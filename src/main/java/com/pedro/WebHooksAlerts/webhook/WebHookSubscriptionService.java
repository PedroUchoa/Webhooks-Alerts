package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.monitor.MonitorService;
import com.pedro.WebHooksAlerts.shared.exception.ResourceNotFoundException;
import com.pedro.WebHooksAlerts.webhook.dtos.CreateWebHookRequest;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookCreatedResponse;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebHookSubscriptionService {

    private final WebHookSubscriptionRepository subscriptionRepository;
    private final MonitorService monitorService;
    private final SecretKeyGenerator secretKeyGenerator;

    @Transactional
    public WebHookCreatedResponse create(UUID monitorId, CreateWebHookRequest request){
        monitorService.ensureExists(monitorId);

        WebHookSubscription subscription = new WebHookSubscription(monitorId,request.callbackUrl(), secretKeyGenerator.generate(), request.events());

        return WebHookCreatedResponse.from(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<WebHookResponse> findByMonitorId(UUID monitorId){
        monitorService.ensureExists(monitorId);

        return subscriptionRepository.findByMonitorId(monitorId).stream()
                .map(WebHookResponse::from).toList();
    }

    @Transactional
    public void delete(UUID monitorId, UUID webhookId){
        monitorService.ensureExists(monitorId);

        WebHookSubscription subscription = subscriptionRepository.findById(webhookId)
                .filter(sub->sub.getMonitorId().equals(monitorId))
                .orElseThrow(()->new ResourceNotFoundException("Webhook Not Found" + webhookId));

        subscriptionRepository.delete(subscription);
    }


}
