package com.quickbite.notification.controller;

import com.quickbite.notification.dto.CreateNotificationRequest;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        Notification notification =
                notificationService.createNotification(
                        request.getRecipientEmail(),
                        request.getType(),
                        request.getMessage(),
                        request.getOrderId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notification);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        authentication.getName()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(
                        authentication.getName()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getUnreadCount(
                        authentication.getName()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        id,
                        authentication.getName()));
    }
    
    
    @PostMapping("/internal")
    public ResponseEntity<Notification> createInternalNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        Notification notification =
                notificationService.createNotification(
                        request.getRecipientEmail(),
                        request.getType(),
                        request.getMessage(),
                        request.getOrderId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notification);
    }
    
}