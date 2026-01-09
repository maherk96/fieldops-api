package com.fieldops.fieldops_api.engineer_availability.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineerAvailabilityDTO {

  private UUID id;

  @NotNull private String availabilityType;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private OffsetDateTime endTime;

  @NotNull private OffsetDateTime lastUpdated;

  private String notes;

  @NotNull private OffsetDateTime startTime;

  @NotNull private UUID engineerUserId;

  @NotNull private UUID organization;
}
