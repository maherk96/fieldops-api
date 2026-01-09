package com.fieldops.fieldops_api.organization.domain;

import com.fieldops.fieldops_api.attachment.domain.Attachment;
import com.fieldops.fieldops_api.audit_log.domain.AuditLog;
import com.fieldops.fieldops_api.customer.domain.Customer;
import com.fieldops.fieldops_api.device_sync_state.domain.DeviceSyncState;
import com.fieldops.fieldops_api.engineer_availability.domain.EngineerAvailability;
import com.fieldops.fieldops_api.engineer_last_location.domain.EngineerLastLocation;
import com.fieldops.fieldops_api.engineer_location.domain.EngineerLocation;
import com.fieldops.fieldops_api.location.domain.Location;
import com.fieldops.fieldops_api.offline_changes_log.domain.OfflineChangesLog;
import com.fieldops.fieldops_api.sync_conflict.domain.SyncConflict;
import com.fieldops.fieldops_api.sync_queue.domain.SyncQueue;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order_assignment.domain.WorkOrderAssignment;
import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import com.fieldops.fieldops_api.work_order_signature.domain.WorkOrderSignature;
import com.fieldops.fieldops_api.work_order_time_entry.domain.WorkOrderTimeEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Organization {

  @Id
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(nullable = false, unique = true, columnDefinition = "text")
  private String subdomain;

  @Column(columnDefinition = "text")
  private String logoUrl;

  @Column(nullable = false, columnDefinition = "text")
  private String settings;

  @Column(nullable = false, columnDefinition = "text")
  private String subscriptionStatus;

  @Column(columnDefinition = "text")
  private String subscriptionPlan;

  @Column(nullable = false)
  private Integer maxEngineers;

  @Column(nullable = false)
  private Integer maxWorkOrdersPerMonth;

  @Column private OffsetDateTime trialEndsAt;

  @OneToMany(mappedBy = "organization")
  private Set<User> organizationUsers = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<Customer> organizationCustomers = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<Location> organizationLocations = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<WorkOrder> organizationWorkOrders = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<WorkOrderAssignment> organizationWorkOrderAssignments = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<WorkOrderEvent> organizationWorkOrderEvents = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<WorkOrderSignature> organizationWorkOrderSignatures = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<WorkOrderTimeEntry> organizationWorkOrderTimeEntries = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<Attachment> organizationAttachments = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<EngineerAvailability> organizationEngineerAvailabilities = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<EngineerLocation> organizationEngineerLocations = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<EngineerLastLocation> organizationEngineerLastLocations = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<DeviceSyncState> organizationDeviceSyncStates = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<SyncConflict> organizationSyncConflicts = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<SyncQueue> organizationSyncQueues = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<OfflineChangesLog> organizationOfflineChangesLogs = new HashSet<>();

  @OneToMany(mappedBy = "organization")
  private Set<AuditLog> organizationAuditLogs = new HashSet<>();

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
