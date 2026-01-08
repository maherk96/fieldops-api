package com.fieldops.fieldops_api.asset.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AssetDTO {

    private UUID id;

    private String assetTag;

    @AssetSerialNumberUnique
    private String serialNumber;

    private String manufacturer;

    private String model;

    @NotNull
    private Integer version;

    @NotNull
    private Long changeVersion;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private OffsetDateTime updatedAt;

    @NotNull
    private UUID location;

}
