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

  @NotNull private String deviceId;

  @NotNull private String operationType;

  @NotNull private String tableName;

  @NotNull private UUID recordId;

  @NotNull private String payload;

  @NotNull private Integer retryCount;

  private String lastError;

  private OffsetDateTime nextRetryAt;

  @NotNull private String status;

  @NotNull private OffsetDateTime createdAt;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private UUID user;
}
