package com.uptimeguard.repository;

import com.uptimeguard.model.CheckResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findByMonitorIdOrderByCheckedAtDesc(Long monitorId, Pageable pageable);
    List<CheckResult> findAllByOrderByCheckedAtDesc(Pageable pageable);
    long countByMonitorIdAndSuccessfulTrue(Long monitorId);
    long countByMonitorId(Long monitorId);
}
