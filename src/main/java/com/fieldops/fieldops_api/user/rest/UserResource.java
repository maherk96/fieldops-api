package com.fieldops.fieldops_api.user.rest;

import com.fieldops.fieldops_api.auth.SecurityConfig;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.service.UserManagementService;
import com.fieldops.fieldops_api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Import;
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
@Import(SecurityConfig.class)
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User management and profile operations")
@SecurityRequirement(name = "bearerAuth")
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
  @Operation(
      summary = "List all users",
      description = "Returns all users in the system. Admin access only.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    return ResponseEntity.ok(userService.findAll());
  }

  /** Admin or self: get a user by id */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
  @Operation(
      summary = "Get user by ID",
      description = "Returns a user by ID. Accessible by admins or the user themselves.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<UserDTO> getUser(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(userService.get(id));
  }

  /**
   * Admin: create a user with password (primary create endpoint).
   *
   * <p>Prefer this over generic POST /api/users.
   */
  @PostMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Create user (admin)",
      description = "Creates a new user with a password. Admin access only.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "User created",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<UserDTO> createUserByAdmin(
      @RequestBody
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "User creation payload",
              content = @Content(schema = @Schema(implementation = CreateUserRequest.class)))
          final CreateUserRequest request) {

    final var createdUser = userManagementService.createUserWithPassword(request);
    return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
  }

  /**
   * Admin or self: update user.
   *
   * <p>IMPORTANT: Field-level rules are enforced in UserManagementService.
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
  @Operation(
      summary = "Update user",
      description =
          "Updates a user. Admins may update role and active status. "
              + "Users may update their own allowed fields only.",
      responses = {
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<UUID> updateUser(
      @PathVariable(name = "id") final UUID id,
      @RequestBody
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Fields to update (null fields are ignored)",
              content = @Content(schema = @Schema(implementation = UserDTO.class)))
          final UserDTO userDTO) {

    userManagementService.updateUser(id, userDTO);
    return ResponseEntity.ok(id);
  }

  /** Admin: delete user */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Delete user",
      description = "Deletes a user by ID. Admin access only.",
      responses = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") final UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Self: get current user's profile.
   *
   * <p>Convenience endpoint that avoids the client needing to know its UUID.
   */
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Get current user profile",
      description = "Returns the authenticated user's profile.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Current user profile",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<UserDTO> me(final Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof UUID)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    final var userId = (UUID) authentication.getPrincipal();
    return ResponseEntity.ok(userService.get(userId));
  }
}
