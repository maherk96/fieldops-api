package com.fieldops.fieldops_api.offline_changes_log.repos;

import com.fieldops.fieldops_api.offline_changes_log.domain.OfflineChangesLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfflineChangesLogRepository extends JpaRepository<OfflineChangesLog, UUID> {

  OfflineChangesLog findFirstByUserId(UUID id);
}
