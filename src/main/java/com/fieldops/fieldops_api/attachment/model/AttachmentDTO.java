package com.fieldops.fieldops_api.attachment.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentDTO {

  private UUID id;

  private String attachmentType;

  @NotNull private OffsetDateTime dateCreated;

  @NotNull private String fileName;

  @NotNull private OffsetDateTime lastUpdated;

  private String mimeType;

  private Long sizeBytes;

  @NotNull private String storageKey;

  @NotNull private String uploadStatus;

  private String uploadedFromDevice;

  private UUID eventId;

  private UUID uploadedByUserId;

  @NotNull private UUID workOrderId;

  @NotNull private UUID organization;
}
