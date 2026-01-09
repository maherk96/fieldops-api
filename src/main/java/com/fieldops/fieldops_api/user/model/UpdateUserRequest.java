package com.fieldops.fieldops_api.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating an existing user.
 *
 * <p>All fields are optional. Only provided fields will be updated.
 *
 * <p>Note: organizationId cannot be changed. Users belong to exactly one organization.
 */
@Getter
@Setter
public class UpdateUserRequest {

  @Email(message = "Email must be valid")
  private String email;

  private String password;

  private String fullName;

  @Pattern(
      regexp = "ADMIN|ENGINEER|DISPATCHER",
      message = "Role must be one of: ADMIN, ENGINEER, DISPATCHER")
  private String role;

  private Boolean active;
}
