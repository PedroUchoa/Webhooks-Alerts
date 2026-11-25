package com.pedro.WebHooksAlerts.webhook;

import com.pedro.WebHooksAlerts.webhook.dtos.CreateWebHookRequest;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookCreatedResponse;
import com.pedro.WebHooksAlerts.webhook.dtos.WebHookResponse;
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
@RequestMapping("/api/monitors/{monitorId}/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Alert Subscription Linked To Monitors")
public class WebHookSubscriptionController {

    private final WebHookSubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Create New Signature From Webhook", description = "Return a secretKey Just With The Response, saves in a securite place and validate the header X-Webhook-Signature")
    public ResponseEntity<WebHookCreatedResponse> create(@PathVariable UUID monitorId, @Valid @RequestBody CreateWebHookRequest request){
        WebHookCreatedResponse created = subscriptionService.create(monitorId,request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List The Webhook Signatures From The Monitor")
    public ResponseEntity<List<WebHookResponse>> findByMonitor(@PathVariable UUID monitorId){
        return ResponseEntity.ok(subscriptionService.findByMonitorId(monitorId));
    }

    @DeleteMapping("/{webhookId}")
    @Operation(summary = "Removes An Webhook Signature From The Monitor")
    public ResponseEntity<Void> delete(@PathVariable UUID monitorId, @PathVariable UUID webhookId){
        subscriptionService.delete(monitorId,webhookId);
        return ResponseEntity.noContent().build();
    }



}
