package com.fieldops.fieldops_api.security;

import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService that loads users by email within an organization context.
 *
 * <p>This service:
 *
 * <ul>
 *   <li>Requires organizationId + email for user lookup (email is unique per organization, not
 *       globally)
 *   <li>Rejects inactive users
 *   <li>Throws UsernameNotFoundException if user not found or inactive
 * </ul>
 *
 * <p>Note: This class implements UserDetailsService for Spring Security compatibility, but the
 * actual lookup uses {@link #loadUserByOrganizationAndEmail(UUID, String)} which should be called
 * explicitly with organization context.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Loads a user by organization ID and email.
   *
   * <p>This is the primary method for user lookup in a multi-tenant context. Email uniqueness is
   * scoped to the organization.
   *
   * @param organizationId the organization ID
   * @param email the user's email
   * @return UserDetails for the user
   * @throws UsernameNotFoundException if user not found or inactive
   */
  @Transactional(readOnly = true)
  public UserDetails loadUserByOrganizationAndEmail(final UUID organizationId, final String email) {
    final User user =
        userRepository
            .findByOrganizationIdAndEmail(organizationId, email)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        String.format(
                            "User not found with email '%s' in organization '%s'",
                            email, organizationId)));

    if (!user.getActive()) {
      throw new UsernameNotFoundException(
          String.format(
              "User with email '%s' in organization '%s' is inactive", email, organizationId));
    }

    return new UserPrincipal(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getOrganization().getId(),
        user.getRole(),
        user.getActive());
  }

  /**
   * Standard UserDetailsService method.
   *
   * <p>This method is not used in our multi-tenant setup since we require organization context.
   * However, it's required by the interface. It throws UnsupportedOperationException to prevent
   * accidental use without organization context.
   *
   * @param username the username (email)
   * @return UserDetails (never returns, always throws)
   * @throws UnsupportedOperationException always, as organization context is required
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    throw new UnsupportedOperationException(
        "loadUserByUsername requires organization context. Use loadUserByOrganizationAndEmail instead.");
  }
}
