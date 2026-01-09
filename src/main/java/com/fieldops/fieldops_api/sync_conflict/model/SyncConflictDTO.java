package com.fieldops.fieldops_api.sync_conflict.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncConflictDTO {

  private UUID id;

  @NotNull private String conflictType;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private String deviceId;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String localData;

  private Integer localVersion;

  @NotNull private UUID recordId;

  private String resolutionStrategy;

  @NotNull private Boolean resolved;

  private OffsetDateTime resolvedAt;

  private String serverData;

  private Integer serverVersion;

  @NotNull private String tableName;

  private UUID resolvedByUserId;

  @NotNull private UUID organization;
}
