package com.fieldops.fieldops_api.user.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for user response (read-only, no password exposed).
 *
 * <p>Used for returning user information in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

  private UUID id;
  private String email;
  private String fullName;
  private String role;
  private Boolean active;
  private UUID organizationId;
  private OffsetDateTime dateCreated;
  private OffsetDateTime lastUpdated;
}
