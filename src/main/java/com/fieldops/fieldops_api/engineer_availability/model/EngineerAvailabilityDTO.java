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

  @NotNull private OffsetDateTime startTime;

  @NotNull private OffsetDateTime endTime;

  private String notes;

  @NotNull private OffsetDateTime createdAt;

  @NotNull private UUID engineerUser;
}
