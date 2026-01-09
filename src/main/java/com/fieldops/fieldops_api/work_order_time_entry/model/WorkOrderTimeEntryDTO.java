package com.fieldops.fieldops_api.work_order_time_entry.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderTimeEntryDTO {

  private UUID id;

  @NotNull private Long changeVersion;

  @NotNull private OffsetDateTime dateCreated;

  private Integer durationMinutes;

  private OffsetDateTime endTime;

  @NotNull private String entryType;

  @NotNull private OffsetDateTime lastUpdated;

  private String notes;

  @NotNull private OffsetDateTime startTime;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private UUID engineerUserId;

  @NotNull private UUID workOrderId;

  @NotNull private UUID organization;
}
