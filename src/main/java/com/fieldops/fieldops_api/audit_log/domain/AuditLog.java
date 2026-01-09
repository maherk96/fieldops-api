package com.fieldops.fieldops_api.audit_log.domain;

import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.user.domain.User;
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
public class AuditLog {

  @Id
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(columnDefinition = "text")
  private String deviceId;

  @Column private String ipAddress;

  @Column(columnDefinition = "text")
  private String newValues;

  @Column(columnDefinition = "text")
  private String oldValues;

  @Column(nullable = false, columnDefinition = "text")
  private String operation;

  @Column(nullable = false)
  private UUID recordId;

  @Column(nullable = false, columnDefinition = "text")
  private String tableName;

  @Column(columnDefinition = "text")
  private String userAgent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by_user_id")
  private User changedByUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
