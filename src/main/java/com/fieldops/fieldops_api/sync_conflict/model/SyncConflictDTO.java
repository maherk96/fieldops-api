package com.fieldops.fieldops_api.sync_conflict.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SyncConflictDTO {

    private UUID id;

    @NotNull
    private String deviceId;

    @NotNull
    private String tableName;

    @NotNull
    private UUID recordId;

    @NotNull
    private String conflictType;

    private Integer localVersion;

    private Integer serverVersion;

    @NotNull
    private String localData;

    private String serverData;

    @NotNull
    private Boolean resolved;

    private String resolutionStrategy;

    private OffsetDateTime resolvedAt;

    @NotNull
    private OffsetDateTime createdAt;

    private UUID resolvedByUser;

}
