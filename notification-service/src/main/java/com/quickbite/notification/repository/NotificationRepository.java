package com.quickbite.notification.repository;

import com.quickbite.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(
            String recipientEmail
    );

    List<Notification> findByRecipientEmailAndReadFalseOrderByCreatedAtDesc(
            String recipientEmail
    );

    long countByRecipientEmailAndReadFalse(String recipientEmail);
}