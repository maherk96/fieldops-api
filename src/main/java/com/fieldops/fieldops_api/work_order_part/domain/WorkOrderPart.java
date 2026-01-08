package com.fieldops.fieldops_api.work_order_part.domain;

import com.fieldops.fieldops_api.parts_catalog.domain.PartsCatalog;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
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
public class WorkOrderPart {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(columnDefinition = "text")
  private String partNumber;

  @Column(nullable = false, columnDefinition = "text")
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal quantity;

  @Column(precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private Long changeVersion;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "work_order_id", nullable = false)
  private WorkOrder workOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "part_id")
  private PartsCatalog part;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by_user_id")
  private User recordedByUser;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
