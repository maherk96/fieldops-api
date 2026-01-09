package com.fieldops.fieldops_api.user.domain;

import com.fieldops.fieldops_api.audit_log.domain.AuditLog;
import com.fieldops.fieldops_api.organization.domain.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false)
  private Boolean active;

  @Column(nullable = false)
  private Long changeVersion;

  @Column(nullable = false, columnDefinition = "text")
  private String email;

  @Column(nullable = false, columnDefinition = "text")
  private String fullName;

  @Column(nullable = false, columnDefinition = "text")
  private String password;

  @Column(nullable = false, columnDefinition = "text")
  private String role;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @Column(nullable = false)
  private Integer version;

  @OneToMany(mappedBy = "changedByUser")
  private Set<AuditLog> changedByUserAuditLogs = new HashSet<>();

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
