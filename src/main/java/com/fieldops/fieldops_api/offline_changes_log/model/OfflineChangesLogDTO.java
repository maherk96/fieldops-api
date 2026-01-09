package com.fieldops.fieldops_api.offline_changes_log.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfflineChangesLogDTO {

  private UUID id;

  @NotNull private String changes;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private String deviceId;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String operation;

  @NotNull private UUID recordId;

  private OffsetDateTime syncedAt;

  @NotNull private String tableName;

  @NotNull private UUID userId;

  @NotNull private UUID organization;
}
