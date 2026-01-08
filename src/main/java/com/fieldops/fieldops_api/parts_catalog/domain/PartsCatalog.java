package com.fieldops.fieldops_api.parts_catalog.domain;

import com.fieldops.fieldops_api.work_order_part.domain.WorkOrderPart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
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
public class PartsCatalog {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, columnDefinition = "text")
  private String partNumber;

  @Column(nullable = false, columnDefinition = "text")
  private String description;

  @Column(precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false)
  private Boolean active;

  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false)
  private Long changeVersion;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "part")
  private Set<WorkOrderPart> partWorkOrderParts = new HashSet<>();

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
