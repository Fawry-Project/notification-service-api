package com.fawry.notificationService;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification ,Integer> {
    List<Notification> findByStatus(Status status);
}
