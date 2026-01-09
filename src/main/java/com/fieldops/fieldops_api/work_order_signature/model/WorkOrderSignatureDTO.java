package com.fieldops.fieldops_api.work_order_signature.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderSignatureDTO {

  private UUID id;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private OffsetDateTime lastUpdated;

  @NotNull private String signatureType;

  @NotNull private OffsetDateTime signedAt;

  @NotNull private String signerName;

  @NotNull private String storageKey;

  @NotNull private UUID workOrderId;

  @NotNull private UUID organization;
}
