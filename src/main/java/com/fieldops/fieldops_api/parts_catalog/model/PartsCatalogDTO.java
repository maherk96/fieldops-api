package com.fieldops.fieldops_api.parts_catalog.model;

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
public class PartsCatalogDTO {

  private UUID id;

  @NotNull private String partNumber;

  @NotNull private String description;

  @Digits(integer = 10, fraction = 2)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal unitPrice;

  @NotNull private Boolean active;

  @NotNull private Integer version;

  @NotNull private Long changeVersion;

  @NotNull private OffsetDateTime createdAt;

  @NotNull private OffsetDateTime updatedAt;
}
