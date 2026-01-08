package com.fieldops.fieldops_api.auth.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CurrentUserResponse {

  private UUID id;
  private String email;
  private String fullName;
  private String role;
  private Boolean active;
}
