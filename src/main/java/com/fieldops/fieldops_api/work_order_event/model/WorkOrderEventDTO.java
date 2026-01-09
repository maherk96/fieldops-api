package com.fieldops.fieldops_api.work_order_event.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderEventDTO {

  private UUID id;

  @NotNull private UUID clientEventId;

  @NotNull private OffsetDateTime dateCreated;

  private String deviceId;

  @NotNull private String eventType;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String payload;

  private OffsetDateTime syncedAt;

  private UUID createdByUserId;

  @NotNull private UUID workOrderId;

  @NotNull private UUID organization;
}
