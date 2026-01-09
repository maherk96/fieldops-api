package com.fieldops.fieldops_api.location.domain;

import com.fieldops.fieldops_api.organization.domain.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
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
public class Location {

  @Id
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(columnDefinition = "text")
  private String addressLine1;

  @Column(columnDefinition = "text")
  private String addressLine2;

  @Column(nullable = false)
  private Long changeVersion;

  @Column(columnDefinition = "text")
  private String city;

  @Column(columnDefinition = "text")
  private String contactPhone;

  @Column(columnDefinition = "text")
  private String country;

  @Column(precision = 10, scale = 7)
  private BigDecimal lat;

  @Column(precision = 10, scale = 7)
  private BigDecimal lng;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String postcode;

  @Column(columnDefinition = "text")
  private String region;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false)
  private UUID customerId;

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
