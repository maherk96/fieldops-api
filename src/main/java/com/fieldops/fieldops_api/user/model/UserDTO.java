package com.fieldops.fieldops_api.user.model;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    private UUID id;

    @NotNull
    private String email;

    @NotNull
    private String fullName;

    @NotNull
    private String role;

    @NotNull
    private Boolean active;

    @NotNull
    private Integer version;

    @NotNull
    private Long changeVersion;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private OffsetDateTime updatedAt;

}
