package com.fieldops.fieldops_api.work_order.domain;

import com.fieldops.fieldops_api.organization.domain.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.OffsetDateTime;
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
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column private OffsetDateTime actualEnd;

  @Column private OffsetDateTime actualStart;

  @Column(nullable = false)
  private Long changeVersion;

  @Column private OffsetDateTime completedAt;

  @Column(columnDefinition = "text")
  private String completionNotes;

  @Column private OffsetDateTime deletedAt;

  @Column(columnDefinition = "text")
  private String description;

  @Column private Integer estimatedDurationMinutes;

  @Column(nullable = false, columnDefinition = "text")
  private String priority;

  @Column private OffsetDateTime scheduledEnd;

  @Column private OffsetDateTime scheduledStart;

  @Column(nullable = false, columnDefinition = "text")
  private String status;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false, columnDefinition = "text")
  private String workOrderNo;

  @Column private UUID assetId;

  @Column private UUID lastModifiedById;

  @Column(nullable = false)
  private UUID locationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
