package com.fieldops.fieldops_api.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for creating a new user.
 *
 * <p>Note: organizationId is automatically set from the authenticated admin's organization. It
 * should not be provided in the request.
 */
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
  @Pattern(
      regexp = "ADMIN|ENGINEER|DISPATCHER",
      message = "Role must be one of: ADMIN, ENGINEER, DISPATCHER")
  private String role;

  @NotNull(message = "Active status is required")
  private Boolean active;
}
