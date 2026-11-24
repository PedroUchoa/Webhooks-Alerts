package com.pedro.WebHooksAlerts.monitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface CheckExecutionRepository extends JpaRepository<CheckExecution, UUID> {

    List<CheckExecution> findTop50ByMonitorIdOrderByCheckedAtDesc(UUID monitorId);

}
