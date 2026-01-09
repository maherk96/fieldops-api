package com.fieldops.fieldops_api.testutil;

import com.fieldops.fieldops_api.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.UUID;

/**
 * Helper class for JWT testing.
 *
 * <p>Provides methods to create test JWT tokens with specified claims.
 */
public final class JwtTestHelper {

  private static final String TEST_SECRET =
      "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-algorithm";
  private static final long TEST_EXPIRATION = 86400000L; // 24 hours

  private JwtTestHelper() {
    // Utility class - prevent instantiation
  }

  /**
   * Creates a JwtUtil instance for testing with a test secret.
   *
   * @return JwtUtil instance
   */
  public static JwtUtil createTestJwtUtil() {
    return new JwtUtil(TEST_SECRET, TEST_EXPIRATION);
  }

  /**
   * Creates a JWT token with the specified claims.
   *
   * @param userId the user ID
   * @param organizationId the organization ID
   * @param role the user role
   * @return the JWT token string
   */
  public static String createJwtToken(
      final UUID userId, final UUID organizationId, final String role) {
    final JwtUtil jwtUtil = createTestJwtUtil();
    return jwtUtil.generateToken(userId, organizationId, role);
  }

  /**
   * Creates a JWT token with a custom expiration time.
   *
   * @param userId the user ID
   * @param organizationId the organization ID
   * @param role the user role
   * @param expirationMs expiration in milliseconds from now
   * @return the JWT token string
   */
  public static String createJwtTokenWithExpiration(
      final UUID userId, final UUID organizationId, final String role, final long expirationMs) {
    final JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, expirationMs);
    return jwtUtil.generateToken(userId, organizationId, role);
  }

  /**
   * Creates an expired JWT token.
   *
   * @param userId the user ID
   * @param organizationId the organization ID
   * @param role the user role
   * @return an expired JWT token string
   */
  public static String createExpiredJwtToken(
      final UUID userId, final UUID organizationId, final String role) {
    // Create a token that expires immediately
    return createJwtTokenWithExpiration(userId, organizationId, role, 0L);
  }

  /**
   * Creates a JWT token with an invalid signature (using different secret).
   *
   * @param userId the user ID
   * @param organizationId the organization ID
   * @param role the user role
   * @return a JWT token with invalid signature
   */
  public static String createInvalidSignatureToken(
      final UUID userId, final UUID organizationId, final String role) {
    final String invalidSecret =
        "different-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";
    final JwtUtil jwtUtil = new JwtUtil(invalidSecret, TEST_EXPIRATION);
    return jwtUtil.generateToken(userId, organizationId, role);
  }

  /**
   * Parses a JWT token using the test secret.
   *
   * @param token the JWT token
   * @return the claims
   */
  public static Claims parseToken(final String token) {
    final JwtUtil jwtUtil = createTestJwtUtil();
    return jwtUtil.parseToken(token);
  }

  /**
   * Gets the test secret used for JWT generation.
   *
   * @return the test secret
   */
  public static String getTestSecret() {
    return TEST_SECRET;
  }

  /**
   * Gets the test expiration time.
   *
   * @return expiration in milliseconds
   */
  public static long getTestExpiration() {
    return TEST_EXPIRATION;
  }
}
