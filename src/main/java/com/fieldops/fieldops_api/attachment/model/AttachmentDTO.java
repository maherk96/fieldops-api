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

    @NotNull
    private String storageKey;

    @NotNull
    private String fileName;

    private String mimeType;

    private Long sizeBytes;

    @NotNull
    private String uploadStatus;

    private String uploadedFromDevice;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private UUID workOrder;

    private UUID event;

    private UUID uploadedByUser;

}
