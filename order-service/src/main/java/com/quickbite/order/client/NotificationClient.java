package com.quickbite.order.client;

import com.quickbite.order.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "NOTIFICATION-SERVICE",
        configuration = FeignConfig.class
)
public interface NotificationClient {

    @PostMapping("/api/notifications/internal")
    void createNotification(
            @RequestBody NotificationRequest request
    );

    record NotificationRequest(
            String recipientEmail,
            String type,
            String message,
            Long orderId
    ) {}
}