package com.uptimeguard.repository;

import com.uptimeguard.model.Incident;
import com.uptimeguard.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findFirstByMonitorIdAndStatusOrderByStartedAtDesc(Long monitorId, IncidentStatus status);
    List<Incident> findAllByOrderByStartedAtDesc();
    List<Incident> findByStatusOrderByStartedAtDesc(IncidentStatus status);
    long countByStatus(IncidentStatus status);
}
