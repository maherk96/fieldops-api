package com.fieldops.fieldops_api.engineer_last_location.model;

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
public class EngineerLastLocationDTO {

    private Long id;

    @NotNull
    @Digits(integer = 10, fraction = 7)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lat;

    @NotNull
    @Digits(integer = 10, fraction = 7)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lng;

    @Digits(integer = 8, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal accuracyMeters;

    @NotNull
    private OffsetDateTime recordedAt;

    @NotNull
    private OffsetDateTime updatedAt;

    private UUID engineerUser;

}
