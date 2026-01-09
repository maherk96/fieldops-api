package com.fieldops.fieldops_api.auth.rest;

import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.auth.model.LoginResponse;
import com.fieldops.fieldops_api.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Provides:
 *
 * <ul>
 *   <li>POST /api/auth/login - User login with organization context
 * </ul>
 */
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

  private final AuthService authService;

  public AuthController(final AuthService authService) {
    this.authService = authService;
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * <p>Supports login with organization context via:
   *
   * <ul>
   *   <li>subdomain: organization subdomain (e.g., "acme" for acme.fieldops.com)
   *   <li>organizationId: direct organization UUID
   * </ul>
   *
   * <p>Either subdomain or organizationId must be provided, but not both.
   *
   * @param loginRequest the login request
   * @return the login response with JWT token
   */
  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
  @SecurityRequirement(name = "No authentication required")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid final LoginRequest loginRequest) {
    final LoginResponse response = authService.login(loginRequest);
    return ResponseEntity.ok(response);
  }
}
