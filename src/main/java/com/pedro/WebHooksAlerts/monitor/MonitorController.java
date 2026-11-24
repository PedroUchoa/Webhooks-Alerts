package com.pedro.WebHooksAlerts.monitor;

import com.pedro.WebHooksAlerts.monitor.dtos.CreateMonitorRequest;
import com.pedro.WebHooksAlerts.monitor.dtos.MonitorResponse;
import com.pedro.WebHooksAlerts.monitor.dtos.UpdateMonitorStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
@Tag(name = "Monitors", description = "Manage All Monitored Endpoints")
public class MonitorController {

    private final MonitorService monitorService;

    @PostMapping
    @Operation(summary = "Register a new URL to be monitored")
    public ResponseEntity<MonitorResponse> create(@Valid @RequestBody CreateMonitorRequest request){
        MonitorResponse created = monitorService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.Id())
                .toUri();
        return ResponseEntity.created(location).body(created);

    }

    @GetMapping
    @Operation(summary = "Lists All Monitored URLs")
    public ResponseEntity<List<MonitorResponse>> findAll(){
        return ResponseEntity.ok(monitorService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Search For a Monitored URL By His Id")
    public ResponseEntity<MonitorResponse> findById(@PathVariable UUID id){
        return ResponseEntity.ok(monitorService.findById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Pause Or Resume an Monitor Execution")
    public ResponseEntity<MonitorResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateMonitorStatusRequest request){
        return ResponseEntity.ok(monitorService.updateActiveStatus(id,request.active()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove an Monitor and His Signatures")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        monitorService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

