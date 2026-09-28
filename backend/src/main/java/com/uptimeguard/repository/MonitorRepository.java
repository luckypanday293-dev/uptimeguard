package com.uptimeguard.repository;

import com.uptimeguard.model.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    List<Monitor> findByActiveTrueOrderByNameAsc();
}
