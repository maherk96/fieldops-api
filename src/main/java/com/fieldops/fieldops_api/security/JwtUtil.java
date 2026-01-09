package com.fieldops.fieldops_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for JWT token operations.
 *
 * <p>Handles:
 *
 * <ul>
 *   <li>Token generation with userId, organizationId, and role claims
 *   <li>Token validation and parsing
 *   <li>Claims extraction
 * </ul>
 */
@Component
public class JwtUtil {

  private final SecretKey secretKey;
  private final long expirationMs;

  public JwtUtil(
      @Value("${jwt.secret}") final String secret,
      @Value("${jwt.expiration}") final long expirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  /**
   * Generates a JWT token for a user.
   *
   * @param userId the user's ID
   * @param organizationId the organization's ID
   * @param role the user's role
   * @return the JWT token string
   */
  public String generateToken(final UUID userId, final UUID organizationId, final String role) {
    final Instant now = Instant.now();
    final Instant expiration = now.plusMillis(expirationMs);

    return Jwts.builder()
        .subject(userId.toString())
        .claim("organizationId", organizationId.toString())
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(secretKey)
        .compact();
  }

  /**
   * Validates and parses a JWT token.
   *
   * @param token the JWT token string
   * @return the claims extracted from the token
   * @throws io.jsonwebtoken.JwtException if the token is invalid
   */
  public Claims parseToken(final String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Extracts the user ID from token claims.
   *
   * @param claims the JWT claims
   * @return the user ID
   */
  public UUID getUserIdFromClaims(final Claims claims) {
    return UUID.fromString(claims.getSubject());
  }

  /**
   * Extracts the organization ID from token claims.
   *
   * @param claims the JWT claims
   * @return the organization ID
   */
  public UUID getOrganizationIdFromClaims(final Claims claims) {
    final String orgIdStr = claims.get("organizationId", String.class);
    if (orgIdStr == null) {
      throw new IllegalStateException("Organization ID not found in token claims");
    }
    return UUID.fromString(orgIdStr);
  }

  /**
   * Extracts the role from token claims.
   *
   * @param claims the JWT claims
   * @return the user's role
   */
  public String getRoleFromClaims(final Claims claims) {
    return claims.get("role", String.class);
  }

  /**
   * Checks if a token is expired.
   *
   * @param claims the JWT claims
   * @return true if expired, false otherwise
   */
  public boolean isTokenExpired(final Claims claims) {
    return claims.getExpiration().before(Date.from(Instant.now()));
  }
}
