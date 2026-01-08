package com.fieldops.fieldops_api.audit_log.repos;

import com.fieldops.fieldops_api.audit_log.domain.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  AuditLog findFirstByChangedByUserId(UUID id);
}
