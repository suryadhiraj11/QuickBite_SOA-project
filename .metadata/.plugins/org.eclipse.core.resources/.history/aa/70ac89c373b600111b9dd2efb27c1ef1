package com.quickbite.notification.service;

import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.entity.NotificationType;
import com.quickbite.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(
            String recipientEmail,
            NotificationType type,
            String message,
            Long orderId) {

        Notification notification = new Notification();

        notification.setRecipientEmail(recipientEmail);
        notification.setType(type);
        notification.setMessage(message);
        notification.setOrderId(orderId);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    public List<Notification> getMyNotifications(
            String recipientEmail) {

        return notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(
                        recipientEmail);
    }

    public List<Notification> getUnreadNotifications(
            String recipientEmail) {

        return notificationRepository
                .findByRecipientEmailAndReadFalseOrderByCreatedAtDesc(
                        recipientEmail);
    }

    public long getUnreadCount(String recipientEmail) {

        return notificationRepository
                .countByRecipientEmailAndReadFalse(
                        recipientEmail);
    }

    public Notification markAsRead(
            Long notificationId,
            String recipientEmail) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"));

        if (!notification.getRecipientEmail()
                .equals(recipientEmail)) {

            throw new org.springframework.security.access
                    .AccessDeniedException(
                            "You are not allowed to access this notification");
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }
}