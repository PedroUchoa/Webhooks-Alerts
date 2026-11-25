package com.pedro.WebHooksAlerts.monitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface MonitorRepository extends JpaRepository<Monitor, UUID> {

    boolean existsById(UUID monitorId);

    @Query(value = """
        SELECT * FROM monitors
        WHERE active = TRUE
          AND next_check_at <= :now
        ORDER BY next_check_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<Monitor> findDueMonitorsForUpdate(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );

}
