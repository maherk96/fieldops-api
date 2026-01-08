package com.fieldops.fieldops_api.location.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LocationDTO {

    private UUID id;

    @NotNull
    private String name;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String region;

    private String postcode;

    private String country;

    private String contactPhone;

    @Digits(integer = 10, fraction = 7)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lat;

    @Digits(integer = 10, fraction = 7)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lng;

    @NotNull
    private Integer version;

    @NotNull
    private Long changeVersion;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private OffsetDateTime updatedAt;

    @NotNull
    private UUID customer;

}
