package com.pedro.WebHooksAlerts.monitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface MonitorRepository extends JpaRepository<Monitor, UUID> {

    boolean existsById(UUID monitorId);

}
