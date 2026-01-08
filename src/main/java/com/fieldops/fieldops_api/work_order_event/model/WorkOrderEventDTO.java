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

  @NotNull private String eventType;

  @NotNull private UUID clientEventId;

  private String deviceId;

  @NotNull private String payload;

  private OffsetDateTime syncedAt;

  @NotNull private OffsetDateTime createdAt;

  @NotNull private UUID workOrder;

  private UUID createdByUser;
}
