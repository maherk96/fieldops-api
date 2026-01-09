package com.fieldops.fieldops_api.auth.service;

import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.auth.model.LoginResponse;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.security.JwtUtil;
import com.fieldops.fieldops_api.security.UserDetailsServiceImpl;
import com.fieldops.fieldops_api.security.UserPrincipal;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 *
 * <p>Handles:
 *
 * <ul>
 *   <li>User login with organization context (subdomain or organizationId)
 *   <li>Password validation using BCrypt
 *   <li>JWT token generation
 * </ul>
 */
@Service
public class AuthService {

  private final UserDetailsServiceImpl userDetailsService;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(
      final UserDetailsServiceImpl userDetailsService,
      final OrganizationRepository organizationRepository,
      final PasswordEncoder passwordEncoder,
      final JwtUtil jwtUtil) {
    this.userDetailsService = userDetailsService;
    this.organizationRepository = organizationRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
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
   * @throws BadCredentialsException if credentials are invalid
   * @throws IllegalArgumentException if organization context is invalid
   */
  @Transactional(readOnly = true)
  public LoginResponse login(final LoginRequest loginRequest) {
    // Resolve organization ID from subdomain or organizationId
    final UUID organizationId = resolveOrganizationId(loginRequest);

    // Load user by organization and email
    final UserDetails userDetails =
        userDetailsService.loadUserByOrganizationAndEmail(organizationId, loginRequest.getEmail());

    // Verify password
    if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    final UserPrincipal userPrincipal = (UserPrincipal) userDetails;

    // Generate JWT token
    final String token =
        jwtUtil.generateToken(
            userPrincipal.getId(), userPrincipal.getOrganizationId(), userPrincipal.getRole());

    return new LoginResponse(
        token,
        userPrincipal.getId(),
        userPrincipal.getUsername(),
        userPrincipal.getRole(),
        userPrincipal.getOrganizationId());
  }

  /**
   * Resolves organization ID from subdomain or organizationId.
   *
   * <p>Either subdomain or organizationId must be provided, but not both.
   *
   * @param loginRequest the login request
   * @return the organization ID
   * @throws IllegalArgumentException if organization context is invalid
   */
  private UUID resolveOrganizationId(final LoginRequest loginRequest) {
    final boolean hasSubdomain =
        loginRequest.getSubdomain() != null && !loginRequest.getSubdomain().trim().isEmpty();
    final boolean hasOrganizationId =
        loginRequest.getOrganizationId() != null
            && !loginRequest.getOrganizationId().trim().isEmpty();

    if (!hasSubdomain && !hasOrganizationId) {
      throw new IllegalArgumentException("Either subdomain or organizationId must be provided");
    }

    if (hasSubdomain && hasOrganizationId) {
      throw new IllegalArgumentException(
          "Only one of subdomain or organizationId should be provided, not both");
    }

    if (hasSubdomain) {
      final Organization organization =
          organizationRepository
              .findBySubdomain(loginRequest.getSubdomain())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          String.format(
                              "Organization not found with subdomain '%s'",
                              loginRequest.getSubdomain())));
      return organization.getId();
    } else {
      try {
        return UUID.fromString(loginRequest.getOrganizationId());
      } catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid organizationId format: '%s'", loginRequest.getOrganizationId()),
            e);
      }
    }
  }
}
