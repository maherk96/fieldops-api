package com.fieldops.fieldops_api.auth;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelper {

  /**
   * Get the current authenticated user's ID.
   *
   * @return the user ID
   * @throws IllegalStateException if no user is authenticated
   */
  public UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UUID) {
      return (UUID) principal;
    }

    throw new IllegalStateException("Invalid authentication principal");
  }

  /**
   * Get the current authenticated user's role.
   *
   * @return the user role (e.g., "ENGINEER", "DISPATCHER", "ADMIN")
   */
  public String getCurrentUserRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    return authentication.getAuthorities().stream()
        .findFirst()
        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
        .orElseThrow(() -> new IllegalStateException("No role found for user"));
  }

  /**
   * Check if the current user has a specific role.
   *
   * @param role the role to check (e.g., "ENGINEER", "DISPATCHER", "ADMIN")
   * @return true if the user has the role, false otherwise
   */
  public boolean hasRole(String role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
  }

  /** Check if the current user is an ENGINEER. */
  public boolean isEngineer() {
    return hasRole("ENGINEER");
  }

  /** Check if the current user is a DISPATCHER. */
  public boolean isDispatcher() {
    return hasRole("DISPATCHER");
  }

  /** Check if the current user is an ADMIN. */
  public boolean isAdmin() {
    return hasRole("ADMIN");
  }

  /** Check if the current user can access all work orders (DISPATCHER or ADMIN). */
  public boolean canAccessAllWorkOrders() {
    return isDispatcher() || isAdmin();
  }
}
