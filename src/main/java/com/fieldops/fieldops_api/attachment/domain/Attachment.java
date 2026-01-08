package com.fieldops.fieldops_api.attachment.domain;

import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
public class Attachment {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(columnDefinition = "text")
  private String attachmentType;

  @Column(nullable = false, columnDefinition = "text")
  private String storageKey;

  @Column(nullable = false, columnDefinition = "text")
  private String fileName;

  @Column(columnDefinition = "text")
  private String mimeType;

  @Column private Long sizeBytes;

  @Column(nullable = false, columnDefinition = "text")
  private String uploadStatus;

  @Column(columnDefinition = "text")
  private String uploadedFromDevice;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "work_order_id", nullable = false)
  private WorkOrder workOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id")
  private WorkOrderEvent event;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by_user_id")
  private User uploadedByUser;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
