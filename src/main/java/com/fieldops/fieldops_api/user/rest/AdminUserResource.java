package com.fieldops.fieldops_api.user.rest;

import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UpdateUserRequest;
import com.fieldops.fieldops_api.user.model.UserResponse;
import com.fieldops.fieldops_api.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin user management endpoints.
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>POST /api/admin/users - Create user (admin only)
 *   <li>GET /api/admin/users - Get all users in organization (admin only)
 *   <li>GET /api/admin/users/{id} - Get user by ID (admin only)
 *   <li>PUT /api/admin/users/{id} - Update user (admin only)
 *   <li>PATCH /api/admin/users/{id}/deactivate - Deactivate user (admin only)
 * </ul>
 *
 * <p>All endpoints:
 *
 * <ul>
 *   <li>Require ADMIN role (enforced by @PreAuthorize)
 *   <li>Enforce organization scoping (users can only be managed within the admin's organization)
 *   <li>Reject cross-tenant access with 403 Forbidden
 * </ul>
 */
@RestController
@RequestMapping(value = "/api/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin User Management", description = "User management endpoints for admins")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserResource {

  private final AdminUserService adminUserService;

  public AdminUserResource(final AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  /**
   * Creates a new user in the authenticated admin's organization.
   *
   * <p>The user is automatically assigned to the admin's organization. Only ADMIN role users can
   * perform this operation.
   *
   * @param createRequest the user creation request
   * @return the created user response
   */
  @PostMapping
  @Operation(
      summary = "Create user",
      description = "Creates a new user in the authenticated admin's organization")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> createUser(
      @RequestBody @Valid final CreateUserRequest createRequest) {
    final UserResponse response = adminUserService.createUser(createRequest);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Gets all users in the authenticated admin's organization.
   *
   * <p>Only ADMIN role users can perform this operation.
   *
   * @return list of users in the organization
   */
  @GetMapping
  @Operation(
      summary = "Get all users",
      description = "Gets all users in the authenticated admin's organization")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    final List<UserResponse> users = adminUserService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  /**
   * Gets a user by ID within the authenticated admin's organization.
   *
   * <p>Only ADMIN role users can perform this operation. Cross-tenant access is rejected with 403
   * Forbidden.
   *
   * @param id the user ID
   * @return the user response
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get user by ID",
      description = "Gets a user by ID within the authenticated admin's organization")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") final UUID id) {
    final UserResponse user = adminUserService.getUser(id);
    return ResponseEntity.ok(user);
  }

  /**
   * Updates an existing user in the authenticated admin's organization.
   *
   * <p>Only ADMIN role users can perform this operation. Only users within the admin's organization
   * can be updated. Cross-tenant access is rejected with 403 Forbidden.
   *
   * @param id the user ID
   * @param updateRequest the user update request
   * @return the updated user response
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update user",
      description = "Updates an existing user in the authenticated admin's organization")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final UpdateUserRequest updateRequest) {
    final UserResponse user = adminUserService.updateUser(id, updateRequest);
    return ResponseEntity.ok(user);
  }

  /**
   * Deactivates a user in the authenticated admin's organization.
   *
   * <p>Only ADMIN role users can perform this operation. Only users within the admin's organization
   * can be deactivated. Cross-tenant access is rejected with 403 Forbidden.
   *
   * @param id the user ID
   * @return the deactivated user response
   */
  @PatchMapping("/{id}/deactivate")
  @Operation(
      summary = "Deactivate user",
      description = "Deactivates a user in the authenticated admin's organization")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> deactivateUser(@PathVariable(name = "id") final UUID id) {
    final UserResponse user = adminUserService.deactivateUser(id);
    return ResponseEntity.ok(user);
  }
}
