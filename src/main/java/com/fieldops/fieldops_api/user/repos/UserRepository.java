package com.fieldops.fieldops_api.user.repos;

import com.fieldops.fieldops_api.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  User findFirstByOrganizationId(UUID id);

  /**
   * Finds a user by organization ID and email.
   *
   * <p>Email uniqueness is scoped to the organization, not globally.
   *
   * @param organizationId the organization ID
   * @param email the user's email
   * @return the user, if found
   */
  Optional<User> findByOrganizationIdAndEmail(UUID organizationId, String email);

  /**
   * Finds all users for a given organization.
   *
   * @param organizationId the organization ID
   * @return list of users in the organization
   */
  List<User> findByOrganizationId(UUID organizationId);

  /**
   * Finds a user by ID and organization ID.
   *
   * <p>Used to ensure tenant isolation - a user can only be accessed if they belong to the
   * specified organization.
   *
   * @param id the user ID
   * @param organizationId the organization ID
   * @return the user, if found and belongs to the organization
   */
  Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

  /**
   * Checks if a user with the given email exists in the organization.
   *
   * @param organizationId the organization ID
   * @param email the user's email
   * @return true if the user exists, false otherwise
   */
  boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);
}
