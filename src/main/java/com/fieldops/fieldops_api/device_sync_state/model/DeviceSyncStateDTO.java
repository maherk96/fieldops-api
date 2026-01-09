package com.fieldops.fieldops_api.device_sync_state.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceSyncStateDTO {

  private UUID id;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private String deviceId;

  @NotNull private Long lastChangeVersion;

  @NotNull private OffsetDateTime lastSyncAt;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String tableName;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private UUID userId;

  @NotNull private UUID organization;
}
