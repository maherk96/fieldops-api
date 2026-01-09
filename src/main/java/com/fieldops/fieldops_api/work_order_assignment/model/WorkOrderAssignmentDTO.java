package com.fieldops.fieldops_api.work_order_assignment.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderAssignmentDTO {

  private UUID id;

  @NotNull private Boolean active;

  @NotNull private OffsetDateTime assignedAt;

  @NotNull private Long changeVersion;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private OffsetDateTime lastUpdated;

  private OffsetDateTime unassignedAt;

  @NotNull private UUID engineerUserId;

  @NotNull private UUID workOrderId;

  @NotNull private UUID organization;
}
