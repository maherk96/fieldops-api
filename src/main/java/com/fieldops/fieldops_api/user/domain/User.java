package com.fieldops.fieldops_api.user.domain;

import com.fieldops.fieldops_api.attachment.domain.Attachment;
import com.fieldops.fieldops_api.audit_log.domain.AuditLog;
import com.fieldops.fieldops_api.device_sync_state.domain.DeviceSyncState;
import com.fieldops.fieldops_api.engineer_availability.domain.EngineerAvailability;
import com.fieldops.fieldops_api.engineer_last_location.domain.EngineerLastLocation;
import com.fieldops.fieldops_api.engineer_location.domain.EngineerLocation;
import com.fieldops.fieldops_api.offline_changes_log.domain.OfflineChangesLog;
import com.fieldops.fieldops_api.sync_conflict.domain.SyncConflict;
import com.fieldops.fieldops_api.sync_queue.domain.SyncQueue;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order_assignment.domain.WorkOrderAssignment;
import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import com.fieldops.fieldops_api.work_order_part.domain.WorkOrderPart;
import com.fieldops.fieldops_api.work_order_time_entry.domain.WorkOrderTimeEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "\"user\"")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, columnDefinition = "text")
  private String email;

  @Column(nullable = false, columnDefinition = "text")
  private String password;

  @Column(nullable = false, columnDefinition = "text")
  private String fullName;

  @Column(nullable = false, columnDefinition = "text")
  private String role;

  @Column(nullable = false)
  private Boolean active;

  @Version
  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false)
  private Long changeVersion;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "lastModifiedBy")
  private Set<WorkOrder> lastModifiedByWorkOrders = new HashSet<>();

  @OneToMany(mappedBy = "engineerUser")
  private Set<WorkOrderAssignment> engineerUserWorkOrderAssignments = new HashSet<>();

  @OneToMany(mappedBy = "createdByUser")
  private Set<WorkOrderEvent> createdByUserWorkOrderEvents = new HashSet<>();

  @OneToMany(mappedBy = "uploadedByUser")
  private Set<Attachment> uploadedByUserAttachments = new HashSet<>();

  @OneToMany(mappedBy = "engineerUser")
  private Set<WorkOrderTimeEntry> engineerUserWorkOrderTimeEntries = new HashSet<>();

  @OneToMany(mappedBy = "recordedByUser")
  private Set<WorkOrderPart> recordedByUserWorkOrderParts = new HashSet<>();

  @OneToMany(mappedBy = "engineerUser")
  private Set<EngineerAvailability> engineerUserEngineerAvailabilities = new HashSet<>();

  @OneToMany(mappedBy = "engineerUser")
  private Set<EngineerLocation> engineerUserEngineerLocations = new HashSet<>();

  @OneToMany(mappedBy = "engineerUser")
  private Set<EngineerLastLocation> engineerUserEngineerLastLocations = new HashSet<>();

  @OneToMany(mappedBy = "resolvedByUser")
  private Set<SyncConflict> resolvedByUserSyncConflicts = new HashSet<>();

  @OneToMany(mappedBy = "user")
  private Set<DeviceSyncState> userDeviceSyncStates = new HashSet<>();

  @OneToMany(mappedBy = "changedByUser")
  private Set<AuditLog> changedByUserAuditLogs = new HashSet<>();

  @OneToMany(mappedBy = "user")
  private Set<SyncQueue> userSyncQueues = new HashSet<>();

  @OneToMany(mappedBy = "user")
  private Set<OfflineChangesLog> userOfflineChangesLogs = new HashSet<>();
}
