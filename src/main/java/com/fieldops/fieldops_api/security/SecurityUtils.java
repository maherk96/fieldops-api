package com.fieldops.fieldops_api.security;

import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing security context information.
 *
 * <p>Provides helper methods to:
 *
 * <ul>
 *   <li>Get the current user's ID from JWT claims
 *   <li>Get the current user's organization ID from JWT claims
 *   <li>Get the current user's role from JWT claims
 * </ul>
 *
 * <p>Note: This utility assumes JWT claims are stored in the authentication details. The JWT filter
 * should set this up properly.
 */
public final class SecurityUtils {

  private SecurityUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Gets the current authenticated user's ID from the security context.
   *
   * @return the user ID, or null if not authenticated
   */
  public static UUID getCurrentUserId() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      try {
        return UUID.fromString(authentication.getName());
      } catch (final IllegalArgumentException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Gets the current authenticated user's organization ID.
   *
   * <p>This method uses TenantContext which is set by the JWT filter. If TenantContext is not set,
   * it tries to extract from authentication details if available.
   *
   * @return the organization ID, or null if not available
   */
  public static UUID getCurrentOrganizationId() {
    // First, try to get from TenantContext (set by JWT filter)
    UUID organizationId = TenantContext.getOrganizationId();
    if (organizationId != null) {
      return organizationId;
    }

    // Fallback: try to get from authentication details (if JWT claims are stored there)
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getDetails() instanceof Claims claims) {
      try {
        return UUID.fromString(claims.get("organizationId", String.class));
      } catch (final Exception e) {
        // Ignore - organizationId not available in claims
      }
    }

    return null;
  }

  /**
   * Gets the current authenticated user's role.
   *
   * @return the user's role (e.g., "ADMIN", "ENGINEER"), or null if not available
   */
  public static String getCurrentUserRole() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getAuthorities() != null) {
      return authentication.getAuthorities().stream()
          .findFirst()
          .map(authority -> authority.getAuthority().replace("ROLE_", ""))
          .orElse(null);
    }
    return null;
  }

  /**
   * Checks if the current user has a specific role.
   *
   * @param role the role to check (e.g., "ADMIN", "ENGINEER")
   * @return true if the user has the role, false otherwise
   */
  public static boolean hasRole(final String role) {
    final String currentRole = getCurrentUserRole();
    return currentRole != null && currentRole.equals(role);
  }

  /**
   * Checks if the current user is an admin.
   *
   * @return true if the user is an admin, false otherwise
   */
  public static boolean isAdmin() {
    return hasRole("ADMIN");
  }
}
