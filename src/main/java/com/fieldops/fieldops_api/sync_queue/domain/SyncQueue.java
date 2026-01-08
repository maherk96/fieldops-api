package com.fieldops.fieldops_api.sync_queue.domain;

import com.fieldops.fieldops_api.user.domain.User;
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
public class SyncQueue {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, columnDefinition = "text")
  private String deviceId;

  @Column(nullable = false, columnDefinition = "text")
  private String operationType;

  @Column(nullable = false, columnDefinition = "text")
  private String tableName;

  @Column(nullable = false)
  private UUID recordId;

  @Column(nullable = false, columnDefinition = "text")
  private String payload;

  @Column(nullable = false)
  private Integer retryCount;

  @Column(columnDefinition = "text")
  private String lastError;

  @Column private OffsetDateTime nextRetryAt;

  @Column(nullable = false, columnDefinition = "text")
  private String status;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private OffsetDateTime dateCreated;

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime lastUpdated;
}
