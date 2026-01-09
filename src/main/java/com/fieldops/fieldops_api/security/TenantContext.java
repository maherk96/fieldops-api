package com.fieldops.fieldops_api.security;

import java.util.UUID;

/**
 * Thread-local context holder for the current tenant (organization).
 *
 * <p>This class provides a way to access the organization ID of the currently authenticated user
 * throughout the request lifecycle. The context is set by the JWT filter and cleared after the
 * request completes.
 */
public class TenantContext {

  private static final ThreadLocal<UUID> organizationIdHolder = new ThreadLocal<>();

  /**
   * Sets the organization ID for the current thread.
   *
   * @param organizationId the organization ID
   */
  public static void setOrganizationId(final UUID organizationId) {
    organizationIdHolder.set(organizationId);
  }

  /**
   * Gets the organization ID for the current thread.
   *
   * @return the organization ID, or null if not set
   */
  public static UUID getOrganizationId() {
    return organizationIdHolder.get();
  }

  /**
   * Clears the organization ID for the current thread.
   *
   * <p>Should be called in a filter or interceptor after the request is processed to prevent memory
   * leaks.
   */
  public static void clear() {
    organizationIdHolder.remove();
  }
}
