package com.fieldops.fieldops_api.device_sync_state.repos;

import com.fieldops.fieldops_api.device_sync_state.domain.DeviceSyncState;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceSyncStateRepository extends JpaRepository<DeviceSyncState, UUID> {

  DeviceSyncState findFirstByUserId(UUID id);
}
