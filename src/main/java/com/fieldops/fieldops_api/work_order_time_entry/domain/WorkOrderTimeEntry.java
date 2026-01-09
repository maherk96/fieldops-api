package com.fieldops.fieldops_api.work_order_time_entry.domain;

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
public class WorkOrderTimeEntry {

  @Id
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false)
  private Long changeVersion;

  @Column private Integer durationMinutes;

  @Column private OffsetDateTime endTime;

  @Column(nullable = false, columnDefinition = "text")
  private String entryType;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(nullable = false)
  private OffsetDateTime startTime;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Column(nullable = false)
  private UUID engineerUserId;

  @Column(nullable = false)
  private UUID workOrderId;

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
