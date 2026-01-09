package com.fieldops.fieldops_api.organization.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationDTO {

  private UUID id;

  @NotNull private String name;

  @NotNull @OrganizationSubdomainUnique private String subdomain;

  private String logoUrl;

  @NotNull private String settings;

  @NotNull private String subscriptionStatus;

  private String subscriptionPlan;

  @NotNull private Integer maxEngineers;

  @NotNull private Integer maxWorkOrdersPerMonth;

  private OffsetDateTime trialEndsAt;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private OffsetDateTime lastUpdated;
}
