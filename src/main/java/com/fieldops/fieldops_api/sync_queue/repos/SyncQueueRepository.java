package com.fieldops.fieldops_api.sync_queue.repos;

import com.fieldops.fieldops_api.sync_queue.domain.SyncQueue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncQueueRepository extends JpaRepository<SyncQueue, UUID> {

  SyncQueue findFirstByUserId(UUID id);
}
