package com.fieldops.fieldops_api.work_order_part.model;

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
public class WorkOrderPartDTO {

    private UUID id;

    private String partNumber;

    @NotNull
    private String description;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal quantity;

    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal unitPrice;

    @NotNull
    private Long changeVersion;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private UUID workOrder;

    private UUID part;

    private UUID recordedByUser;

}
