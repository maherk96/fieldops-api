package com.fieldops.fieldops_api.auth.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for login response.
 *
 * <p>Contains:
 *
 * <ul>
 *   <li>JWT token for subsequent authenticated requests
 *   <li>User information (id, email, role, organizationId)
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String token;
  private UUID userId;
  private String email;
  private String role;
  private UUID organizationId;
}
