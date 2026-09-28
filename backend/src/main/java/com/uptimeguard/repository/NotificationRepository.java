package com.uptimeguard.repository;

import com.uptimeguard.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByReadFlagFalse();
}
