package com.fieldops.fieldops_api.sync_queue.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncQueueDTO {

  private UUID id;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private String deviceId;

  private String lastError;

  @NotNull private OffsetDateTime lastUpdated;

  private OffsetDateTime nextRetryAt;

  @NotNull private String operationType;

  @NotNull private String payload;

  @NotNull private UUID recordId;

  @NotNull private Integer retryCount;

  @NotNull private String status;

  @NotNull private String tableName;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private UUID userId;

  @NotNull private UUID organization;
}
