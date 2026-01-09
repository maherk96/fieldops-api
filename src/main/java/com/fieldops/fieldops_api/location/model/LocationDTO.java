package com.fieldops.fieldops_api.location.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationDTO {

  private UUID id;

  private String addressLine1;

  private String addressLine2;

  @NotNull private Long changeVersion;

  private String city;

  private String contactPhone;

  private String country;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private OffsetDateTime lastUpdated;

  @Digits(integer = 10, fraction = 7)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal lat;

  @Digits(integer = 10, fraction = 7)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal lng;

  @NotNull private String name;

  private String postcode;

  private String region;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private Integer version;

  @NotNull private UUID customerId;

  @NotNull private UUID organization;
}
