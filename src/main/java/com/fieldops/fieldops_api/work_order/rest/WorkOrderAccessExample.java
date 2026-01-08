package com.fieldops.fieldops_api.work_order.rest;

import com.fieldops.fieldops_api.auth.AuthenticationHelper;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller showing role-based access control for work orders.
 *
 * <p>Rules: - ENGINEER: Can only access their own work orders - DISPATCHER/ADMIN: Can access all
 * work orders
 */
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderAccessExample {

  private final AuthenticationHelper authHelper;

  public WorkOrderAccessExample(final AuthenticationHelper authHelper) {
    this.authHelper = authHelper;
  }

  /** Example: Only DISPATCHER and ADMIN can access this endpoint. */
  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
  public String getAllWorkOrders() {
    return "All work orders - only accessible by DISPATCHER or ADMIN";
  }

  /**
   * Example: Check access programmatically. ENGINEER can only see their own work orders.
   * DISPATCHER/ADMIN can see all.
   */
  @GetMapping("/{id}")
  public String getWorkOrder(@PathVariable UUID id) {
    // Get current user
    UUID currentUserId = authHelper.getCurrentUserId();

    // In a real implementation, you would:
    // 1. Fetch the work order from database
    // 2. Check if it's assigned to the current user
    // 3. If user is ENGINEER, verify they own it
    // 4. If user is DISPATCHER/ADMIN, allow access

    if (authHelper.canAccessAllWorkOrders()) {
      return "Work order " + id + " - accessed by DISPATCHER/ADMIN";
    } else {
      // For ENGINEER, check if work order is assigned to them
      // This is pseudo-code - implement based on your WorkOrder entity
      return "Work order " + id + " - accessed by ENGINEER " + currentUserId;
    }
  }

  /** Example: Only ADMIN can delete work orders. */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin-only")
  public String adminOnlyEndpoint() {
    return "Admin only endpoint";
  }
}
