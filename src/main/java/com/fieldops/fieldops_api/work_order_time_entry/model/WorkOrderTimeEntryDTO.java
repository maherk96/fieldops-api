package com.fieldops.fieldops_api.work_order_time_entry.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class WorkOrderTimeEntryDTO {

    private UUID id;

    @NotNull
    private String entryType;

    @NotNull
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private Integer durationMinutes;

    private String notes;

    @NotNull
    private Long changeVersion;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private OffsetDateTime updatedAt;

    @NotNull
    private UUID workOrder;

    @NotNull
    private UUID engineerUser;

}
