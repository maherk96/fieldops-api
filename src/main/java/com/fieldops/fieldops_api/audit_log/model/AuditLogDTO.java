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

  @NotNull private OffsetDateTime changedAt;

  @NotNull private OffsetDateTime dateCreated;

  private String deviceId;

  @Size(max = 255)
  private String ipAddress;

  @NotNull private OffsetDateTime lastUpdated;

  private String newValues;

  private String oldValues;

  @NotNull private String operation;

  @NotNull private UUID recordId;

  @NotNull private String tableName;

  private String userAgent;

  private UUID changedByUser;

  private UUID organization;
}
