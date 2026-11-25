package com.pedro.WebHooksAlerts.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebHookDeliveryRepository extends JpaRepository<WebHookDelivery, UUID> {
}
