package com.fieldops.fieldops_api.work_order.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderDTO {

  private UUID id;

  @NotNull private String workOrderNo;

  @NotNull private String title;

  private String description;

  @NotNull private String priority;

  @NotNull private String status;

  private OffsetDateTime scheduledStart;

  private OffsetDateTime scheduledEnd;

  private Integer estimatedDurationMinutes;

  private OffsetDateTime actualStart;

  private OffsetDateTime actualEnd;

  private OffsetDateTime completedAt;

  private String completionNotes;

  private OffsetDateTime deletedAt;

  @NotNull private Integer version;

  @NotNull private Long changeVersion;

  @NotNull private OffsetDateTime createdAt;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private UUID location;

  private UUID asset;

  private UUID lastModifiedBy;
}
