package com.fieldops.fieldops_api.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Role is required")
  private String role;

  @NotNull(message = "Active status is required")
  private Boolean active;
}
