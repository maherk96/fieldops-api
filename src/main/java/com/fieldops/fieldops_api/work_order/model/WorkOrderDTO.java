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

  private OffsetDateTime actualEnd;

  private OffsetDateTime actualStart;

  @NotNull private Long changeVersion;

  private OffsetDateTime completedAt;

  private String completionNotes;

  @NotNull private OffsetDateTime dateCreated;

  private OffsetDateTime deletedAt;

  private String description;

  private Integer estimatedDurationMinutes;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String priority;

  private OffsetDateTime scheduledEnd;

  private OffsetDateTime scheduledStart;

  @NotNull private String status;

  @NotNull private String title;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private Integer version;

  @NotNull private String workOrderNo;

  private UUID assetId;

  private UUID lastModifiedById;

  @NotNull private UUID locationId;

  @NotNull private UUID organization;
}
