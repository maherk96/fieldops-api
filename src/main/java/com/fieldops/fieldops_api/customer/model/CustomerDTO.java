package com.fieldops.fieldops_api.customer.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO {

  private UUID id;

  @NotNull private Long changeVersion;

  @NotNull private OffsetDateTime dateCreated;

  private String externalRef;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String name;

  private String phone;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private Integer version;

  @NotNull private UUID organization;
}
