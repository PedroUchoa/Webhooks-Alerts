package com.pedro.WebHooksAlerts.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebHookDeliveryRepository extends JpaRepository<WebHookDelivery, UUID> {

    @Query("SELECT d FROM WebhookDelivery d JOIN FETCH d.subscription WHERE d.id = :id")
    Optional<WebHookDelivery> findByIdWithSubscription(@Param("id") UUID id);

    List<WebHookDelivery> findTop50BySubscriptionMonitorIdOrderByCreatedAtDesc(UUID monitorId);

}
