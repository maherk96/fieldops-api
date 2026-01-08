package com.fieldops.fieldops_api.sync_conflict.repos;

import com.fieldops.fieldops_api.sync_conflict.domain.SyncConflict;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncConflictRepository extends JpaRepository<SyncConflict, UUID> {

  SyncConflict findFirstByResolvedByUserId(UUID id);
}
