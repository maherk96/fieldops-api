package com.fieldops.fieldops_api.work_order.domain;

import com.fieldops.fieldops_api.asset.domain.Asset;
import com.fieldops.fieldops_api.attachment.domain.Attachment;
import com.fieldops.fieldops_api.location.domain.Location;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.work_order_assignment.domain.WorkOrderAssignment;
import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import com.fieldops.fieldops_api.work_order_part.domain.WorkOrderPart;
import com.fieldops.fieldops_api.work_order_signature.domain.WorkOrderSignature;
import com.fieldops.fieldops_api.work_order_time_entry.domain.WorkOrderTimeEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class WorkOrder {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, columnDefinition = "text")
  private String workOrderNo;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(nullable = false, columnDefinition = "text")
  private String priority;

  @Column(nullable = false, columnDefinition = "text")
  private String status;

  @Column private OffsetDateTime scheduledStart;

  @Column private OffsetDateTime scheduledEnd;

  @Column private Integer estimatedDurationMinutes;

  @Column private OffsetDateTime actualStart;

  @Column private OffsetDateTime actualEnd;

  @Column private OffsetDateTime completedAt;

  @Column(columnDefinition = "text")
  private String completionNotes;

  @Column private OffsetDateTime deletedAt;

  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false)
  private Long changeVersion;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  private Location location;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_id")
  private Asset asset;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by_id")
  private User lastModifiedBy;

  @OneToMany(mappedBy = "workOrder")
  private Set<WorkOrderAssignment> workOrderWorkOrderAssignments = new HashSet<>();

  @OneToMany(mappedBy = "workOrder")
  private Set<WorkOrderEvent> workOrderWorkOrderEvents = new HashSet<>();

  @OneToMany(mappedBy = "workOrder")
  private Set<Attachment> workOrderAttachments = new HashSet<>();

  @OneToMany(mappedBy = "workOrder")
  private Set<WorkOrderTimeEntry> workOrderWorkOrderTimeEntries = new HashSet<>();

  @OneToMany(mappedBy = "workOrder")
  private Set<WorkOrderPart> workOrderWorkOrderParts = new HashSet<>();

  @OneToMany(mappedBy = "workOrder")
  private Set<WorkOrderSignature> workOrderWorkOrderSignatures = new HashSet<>();

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
