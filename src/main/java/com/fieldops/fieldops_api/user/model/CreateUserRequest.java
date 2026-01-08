package com.fieldops.fieldops_api.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model used to create a new user.
 *
 * <p>This DTO is typically used by REST controllers to accept user creation payloads and is
 * validated using Jakarta Bean Validation annotations.
 */
@Getter
@Setter
public class CreateUserRequest {

  /**
   * User email address.
   *
   * <p>Must be present and a valid email format.
   */
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  /**
   * Raw user password.
   *
   * <p>This value should be encoded before being persisted.
   */
  @NotBlank(message = "Password is required")
  private String password;

  /** Optional full name of the user. */
  private String fullName;

  /**
   * Role assigned to the user (e.g. ADMIN, USER).
   *
   * <p>Role validation should be enforced at the service or domain layer.
   */
  @NotBlank(message = "Role is required")
  private String role;

  /**
   * Indicates whether the user account should be active.
   *
   * <p>If {@code null}, the service layer may apply a default value.
   */
  private Boolean active;
}
