package com.fieldops.fieldops_api.audit_log.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuditLogDTO {

    private UUID id;

    @NotNull
    private String tableName;

    @NotNull
    private UUID recordId;

    @NotNull
    private String operation;

    private String oldValues;

    private String newValues;

    @NotNull
    private OffsetDateTime changedAt;

    @Size(max = 255)
    private String ipAddress;

    private String userAgent;

    private String deviceId;

    private UUID changedByUser;

}
