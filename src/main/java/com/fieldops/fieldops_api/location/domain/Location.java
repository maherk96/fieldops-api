package com.fieldops.fieldops_api.location.domain;

import com.fieldops.fieldops_api.asset.domain.Asset;
import com.fieldops.fieldops_api.customer.domain.Customer;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Location {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    @Column(columnDefinition = "text")
    private String addressLine1;

    @Column(columnDefinition = "text")
    private String addressLine2;

    @Column(columnDefinition = "text")
    private String city;

    @Column(columnDefinition = "text")
    private String region;

    @Column(columnDefinition = "text")
    private String postcode;

    @Column(columnDefinition = "text")
    private String country;

    @Column(columnDefinition = "text")
    private String contactPhone;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Long changeVersion;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "location")
    private Set<Asset> locationAssets = new HashSet<>();

    @OneToMany(mappedBy = "location")
    private Set<WorkOrder> locationWorkOrders = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}
