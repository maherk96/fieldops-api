package com.fieldops.fieldops_api.testutil;

import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.user.domain.User;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Helper class for creating test data entities.
 *
 * <p>Provides factory methods for creating test entities with sensible defaults.
 */
public final class TestDataHelper {

  private TestDataHelper() {
    // Utility class - prevent instantiation
  }

  /**
   * Creates a test organization with default values.
   *
   * @param subdomain the organization subdomain
   * @return the test organization
   */
  public static Organization createTestOrganization(final String subdomain) {
    final Organization org = new Organization();
    org.setId(UUID.randomUUID());
    org.setName("Test Organization " + subdomain);
    org.setSubdomain(subdomain);
    org.setLogoUrl("https://example.com/logo.png");
    org.setSettings("{}");
    org.setSubscriptionStatus("ACTIVE");
    org.setSubscriptionPlan("PRO");
    org.setMaxEngineers(100);
    org.setMaxWorkOrdersPerMonth(1000);
    org.setDateCreated(OffsetDateTime.now());
    org.setLastUpdated(OffsetDateTime.now());
    return org;
  }

  /**
   * Creates a test organization with a specific ID.
   *
   * @param id the organization ID
   * @param subdomain the organization subdomain
   * @return the test organization
   */
  public static Organization createTestOrganization(final UUID id, final String subdomain) {
    final Organization org = createTestOrganization(subdomain);
    org.setId(id);
    return org;
  }

  /**
   * Creates a test user with default values.
   *
   * @param email the user email
   * @param organization the user's organization
   * @param role the user role (ADMIN, ENGINEER, DISPATCHER)
   * @param passwordEncoder the password encoder (for encoding password)
   * @return the test user
   */
  public static User createTestUser(
      final String email,
      final Organization organization,
      final String role,
      final PasswordEncoder passwordEncoder) {
    final User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    user.setFullName("Test User");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setRole(role);
    user.setActive(true);
    user.setOrganization(organization);
    user.setChangeVersion(0L);
    user.setVersion(1);
    user.setUpdatedAt(OffsetDateTime.now());
    user.setDateCreated(OffsetDateTime.now());
    user.setLastUpdated(OffsetDateTime.now());
    return user;
  }

  /**
   * Creates a test user with a specific ID.
   *
   * @param id the user ID
   * @param email the user email
   * @param organization the user's organization
   * @param role the user role
   * @param passwordEncoder the password encoder
   * @return the test user
   */
  public static User createTestUser(
      final UUID id,
      final String email,
      final Organization organization,
      final String role,
      final PasswordEncoder passwordEncoder) {
    final User user = createTestUser(email, organization, role, passwordEncoder);
    user.setId(id);
    return user;
  }

  /**
   * Creates a test user with specific active status.
   *
   * @param email the user email
   * @param organization the user's organization
   * @param role the user role
   * @param active whether the user is active
   * @param passwordEncoder the password encoder
   * @return the test user
   */
  public static User createTestUser(
      final String email,
      final Organization organization,
      final String role,
      final boolean active,
      final PasswordEncoder passwordEncoder) {
    final User user = createTestUser(email, organization, role, passwordEncoder);
    user.setActive(active);
    return user;
  }
}
