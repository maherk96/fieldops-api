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

  @NotNull private OffsetDateTime assignedAt;

  private OffsetDateTime unassignedAt;

  @NotNull private Boolean active;

  @NotNull private Long changeVersion;

  @NotNull private UUID workOrder;

  @NotNull private UUID engineerUser;
}
