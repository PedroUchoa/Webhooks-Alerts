package com.pedro.WebHooksAlerts.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebHookSubscriptionRepository extends JpaRepository<WebHookSubscription, UUID> {

    List<WebHookSubscription> findByMonitorId(UUID monitorId);
    List<WebHookSubscription> findByMonitorIdAndActiveTrue(UUID monitorId);

}
