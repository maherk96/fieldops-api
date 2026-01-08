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

  @NotNull private String deviceId;

  @NotNull private String tableName;

  @NotNull private UUID recordId;

  @NotNull private String operation;

  @NotNull private String changes;

  private OffsetDateTime syncedAt;

  @NotNull private OffsetDateTime createdAt;

  @NotNull private UUID user;
}
