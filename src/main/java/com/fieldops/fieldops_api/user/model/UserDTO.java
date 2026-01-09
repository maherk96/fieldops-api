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

  @NotNull private Boolean active;

  @NotNull private Long changeVersion;

  @NotNull private String email;

  @NotNull private String fullName;

  @NotNull private String password;

  @NotNull private String role;

  @NotNull private OffsetDateTime updatedAt;

  @NotNull private Integer version;

  @NotNull private UUID organization;
}
