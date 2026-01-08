package com.fieldops.fieldops_api.user.rest;

import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.service.UserManagementService;
import com.fieldops.fieldops_api.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserResource {

  private final UserService userService;
  private final UserManagementService userManagementService;

  public UserResource(
          final UserService userService, final UserManagementService userManagementService) {
    this.userService = userService;
    this.userManagementService = userManagementService;
  }

  /** Admin: list all users */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    return ResponseEntity.ok(userService.findAll());
  }

  /** Admin or self: get a user by id */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
  public ResponseEntity<UserDTO> getUser(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(userService.get(id));
  }

  /**
   * Admin: create a user with password (primary create endpoint).
   * Prefer this over the generic POST /api/users.
   */
  @PostMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> createUserByAdmin(
          @RequestBody @Valid final CreateUserRequest request) {
    final UserDTO createdUser = userManagementService.createUserWithPassword(request);
    return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
  }

  /**
   * Admin or self: update user.
   *
   * IMPORTANT: Enforce field-level rules in UserManagementService:
   * - Self can only update allowed fields (e.g., fullName)
   * - Self cannot change role/active/version/changeVersion, etc.
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
  public ResponseEntity<UUID> updateUser(
          @PathVariable(name = "id") final UUID id, @RequestBody @Valid final UserDTO userDTO) {
    userManagementService.updateUser(id, userDTO);
    return ResponseEntity.ok(id);
  }

  /** Admin: delete user */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") final UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Self: get current user's profile (convenience endpoint).
   * This avoids the client needing to know its UUID.
   */
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserDTO> me(final Authentication authentication) {
    if (authentication == null
            || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof UUID)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    final UUID userId = (UUID) authentication.getPrincipal();
    return ResponseEntity.ok(userService.get(userId));
  }
}