package com.fieldops.fieldops_api.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for login request.
 *
 * <p>Supports login with organization context via:
 *
 * <ul>
 *   <li>subdomain: organization subdomain (e.g., "acme" for acme.fieldops.com)
 *   <li>organizationId: direct organization UUID
 * </ul>
 *
 * <p>Either subdomain or organizationId must be provided, but not both.
 */
@Getter
@Setter
public class LoginRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  /**
   * Organization subdomain (e.g., "acme" for acme.fieldops.com).
   *
   * <p>Mutually exclusive with organizationId.
   */
  private String subdomain;

  /**
   * Organization UUID.
   *
   * <p>Mutually exclusive with subdomain.
   */
  private String organizationId;
}
