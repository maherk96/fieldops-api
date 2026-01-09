package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.security.TenantContext;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UpdateUserRequest;
import com.fieldops.fieldops_api.user.model.UserResponse;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.ForbiddenException;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin user management operations.
 *
 * <p>This service enforces tenant boundaries:
 *
 * <ul>
 *   <li>Users can only be managed within the authenticated admin's organization
 *   <li>Cross-tenant access is rejected with 403 Forbidden
 *   <li>User creation automatically assigns the admin's organization
 *   <li>Repository queries always filter by organizationId
 * </ul>
 *
 * <p>Only ADMIN role users can perform these operations (enforced by @PreAuthorize in the
 * controller).
 */
@Service
public class AdminUserService {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserService(
      final UserRepository userRepository,
      final OrganizationRepository organizationRepository,
      final PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Creates a new user in the authenticated admin's organization.
   *
   * <p>The user is automatically assigned to the admin's organization. The organizationId from the
   * request (if provided) is ignored.
   *
   * @param createRequest the user creation request
   * @return the created user response
   * @throws IllegalArgumentException if email already exists in the organization
   */
  @Transactional
  public UserResponse createUser(final CreateUserRequest createRequest) {
    final UUID organizationId = TenantContext.getOrganizationId();
    if (organizationId == null) {
      throw new IllegalStateException("Organization context not available");
    }

    // Check if email already exists in the organization
    if (userRepository.existsByOrganizationIdAndEmail(organizationId, createRequest.getEmail())) {
      throw new IllegalArgumentException(
          String.format(
              "User with email '%s' already exists in organization '%s'",
              createRequest.getEmail(), organizationId));
    }

    final Organization organization =
        organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not found"));

    final User user = new User();
    user.setEmail(createRequest.getEmail());
    user.setPassword(passwordEncoder.encode(createRequest.getPassword()));
    user.setFullName(createRequest.getFullName());
    user.setRole(createRequest.getRole());
    user.setActive(createRequest.getActive());
    user.setOrganization(organization);
    user.setChangeVersion(0L);
    user.setVersion(1);
    user.setUpdatedAt(OffsetDateTime.now());

    final User savedUser = userRepository.save(user);

    return mapToResponse(savedUser);
  }

  /**
   * Updates an existing user in the authenticated admin's organization.
   *
   * <p>Only users within the admin's organization can be updated. Cross-tenant access is rejected.
   *
   * @param userId the user ID
   * @param updateRequest the user update request
   * @return the updated user response
   * @throws ForbiddenException if the user belongs to a different organization
   * @throws NotFoundException if the user is not found
   */
  @Transactional
  public UserResponse updateUser(final UUID userId, final UpdateUserRequest updateRequest) {
    final UUID organizationId = TenantContext.getOrganizationId();
    if (organizationId == null) {
      throw new IllegalStateException("Organization context not available");
    }

    // Find user within the organization (enforces tenant boundary)
    final User user =
        userRepository
            .findByIdAndOrganizationId(userId, organizationId)
            .orElseThrow(
                () -> {
                  // Check if user exists in another organization (cross-tenant access attempt)
                  if (userRepository.findById(userId).isPresent()) {
                    throw new ForbiddenException(
                        "Cannot access or modify users from other organizations");
                  }
                  return new NotFoundException("User not found");
                });

    // Update fields if provided
    if (updateRequest.getEmail() != null) {
      // Check if email already exists in the organization (excluding current user)
      if (!updateRequest.getEmail().equals(user.getEmail())
          && userRepository.existsByOrganizationIdAndEmail(
              organizationId, updateRequest.getEmail())) {
        throw new IllegalArgumentException(
            String.format(
                "User with email '%s' already exists in organization '%s'",
                updateRequest.getEmail(), organizationId));
      }
      user.setEmail(updateRequest.getEmail());
    }

    if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
    }

    if (updateRequest.getFullName() != null) {
      user.setFullName(updateRequest.getFullName());
    }

    if (updateRequest.getRole() != null) {
      user.setRole(updateRequest.getRole());
    }

    if (updateRequest.getActive() != null) {
      user.setActive(updateRequest.getActive());
    }

    user.setUpdatedAt(OffsetDateTime.now());
    user.setVersion(user.getVersion() + 1);

    final User savedUser = userRepository.save(user);

    return mapToResponse(savedUser);
  }

  /**
   * Deactivates a user in the authenticated admin's organization.
   *
   * <p>Only users within the admin's organization can be deactivated. Cross-tenant access is
   * rejected.
   *
   * @param userId the user ID
   * @return the deactivated user response
   * @throws ForbiddenException if the user belongs to a different organization
   * @throws NotFoundException if the user is not found
   */
  @Transactional
  public UserResponse deactivateUser(final UUID userId) {
    final UUID organizationId = TenantContext.getOrganizationId();
    if (organizationId == null) {
      throw new IllegalStateException("Organization context not available");
    }

    // Find user within the organization (enforces tenant boundary)
    final User user =
        userRepository
            .findByIdAndOrganizationId(userId, organizationId)
            .orElseThrow(
                () -> {
                  // Check if user exists in another organization (cross-tenant access attempt)
                  if (userRepository.findById(userId).isPresent()) {
                    throw new ForbiddenException(
                        "Cannot access or modify users from other organizations");
                  }
                  return new NotFoundException("User not found");
                });

    user.setActive(false);
    user.setUpdatedAt(OffsetDateTime.now());
    user.setVersion(user.getVersion() + 1);

    final User savedUser = userRepository.save(user);

    return mapToResponse(savedUser);
  }

  /**
   * Gets all users in the authenticated admin's organization.
   *
   * @return list of users in the organization
   */
  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    final UUID organizationId = TenantContext.getOrganizationId();
    if (organizationId == null) {
      throw new IllegalStateException("Organization context not available");
    }

    return userRepository.findByOrganizationId(organizationId).stream()
        .map(this::mapToResponse)
        .toList();
  }

  /**
   * Gets a user by ID within the authenticated admin's organization.
   *
   * @param userId the user ID
   * @return the user response
   * @throws ForbiddenException if the user belongs to a different organization
   * @throws NotFoundException if the user is not found
   */
  @Transactional(readOnly = true)
  public UserResponse getUser(final UUID userId) {
    final UUID organizationId = TenantContext.getOrganizationId();
    if (organizationId == null) {
      throw new IllegalStateException("Organization context not available");
    }

    // Find user within the organization (enforces tenant boundary)
    final User user =
        userRepository
            .findByIdAndOrganizationId(userId, organizationId)
            .orElseThrow(
                () -> {
                  // Check if user exists in another organization (cross-tenant access attempt)
                  if (userRepository.findById(userId).isPresent()) {
                    throw new ForbiddenException(
                        "Cannot access or modify users from other organizations");
                  }
                  return new NotFoundException("User not found");
                });

    return mapToResponse(user);
  }

  /**
   * Maps a User entity to a UserResponse DTO.
   *
   * @param user the user entity
   * @return the user response DTO
   */
  private UserResponse mapToResponse(final User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .role(user.getRole())
        .active(user.getActive())
        .organizationId(user.getOrganization().getId())
        .dateCreated(user.getDateCreated())
        .lastUpdated(user.getLastUpdated())
        .build();
  }
}
