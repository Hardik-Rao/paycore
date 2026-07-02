package com.paycore.notification.web.api;

import com.paycore.notification.domain.WebhookDelivery;
import com.paycore.notification.domain.WebhookSubscription;
import com.paycore.notification.repository.WebhookDeliveryRepository;
import com.paycore.notification.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/webhooks") @RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;
    private final WebhookDeliveryRepository deliveryRepository;

    @PostMapping("/subscriptions") public WebhookSubscription create(@RequestBody WebhookSubscription sub) {
        return webhookService.create(sub);
    }
    @GetMapping("/subscriptions") public List<WebhookSubscription> list() { return webhookService.list(); }
    @DeleteMapping("/subscriptions/{id}") public void delete(@PathVariable UUID id) { webhookService.delete(id); }

    @PostMapping("/subscriptions/{id}/test")
    public void test(@PathVariable UUID id) {
        webhookService.handleEvent("test", UUID.randomUUID(), Map.of("test", true, "subscriptionId", id.toString()));
    }

    @GetMapping("/deliveries")
    public Page<WebhookDelivery> deliveries(@RequestParam(required = false) String status, Pageable pageable) {
        return status != null ? deliveryRepository.findByStatus(status, pageable) : deliveryRepository.findAll(pageable);
    }

    @PostMapping("/deliveries/{id}/retry") public WebhookDelivery retry(@PathVariable UUID id) {
        return webhookService.retry(id);
    }

    @GetMapping("/stats") public Map<String, Object> stats() { return webhookService.stats(); }
}
