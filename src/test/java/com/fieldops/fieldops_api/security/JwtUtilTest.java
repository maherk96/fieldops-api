package com.fieldops.fieldops_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fieldops.fieldops_api.testutil.JwtTestHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JwtUtil.
 *
 * <p>Tests JWT token generation, validation, and claims extraction.
 */
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;
  private UUID userId;
  private UUID organizationId;
  private String role;

  @BeforeEach
  void setUp() {
    jwtUtil = JwtTestHelper.createTestJwtUtil();
    userId = UUID.randomUUID();
    organizationId = UUID.randomUUID();
    role = "ADMIN";
  }

  @Test
  @DisplayName("generateToken with valid claims returns token with all claims")
  void generateToken_WithValidClaims_ReturnsTokenWithAllClaims() {
    // Given: valid claims

    // When: token is generated
    final String token = jwtUtil.generateToken(userId, organizationId, role);

    // Then: token is not null and contains all claims
    assertThat(token).isNotNull().isNotEmpty();

    final Claims claims = jwtUtil.parseToken(token);
    assertThat(claims.getSubject()).isEqualTo(userId.toString());
    assertThat(claims.get("organizationId", String.class)).isEqualTo(organizationId.toString());
    assertThat(claims.get("role", String.class)).isEqualTo(role);
    assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now()));
  }

  @Test
  @DisplayName("generateToken with different orgIds produces different tokens")
  void generateToken_WithDifferentOrgIds_ProducesDifferentTokens() {
    // Given: two different organization IDs
    final UUID orgId1 = UUID.randomUUID();
    final UUID orgId2 = UUID.randomUUID();

    // When: tokens are generated for each org
    final String token1 = jwtUtil.generateToken(userId, orgId1, role);
    final String token2 = jwtUtil.generateToken(userId, orgId2, role);

    // Then: tokens are different
    assertThat(token1).isNotEqualTo(token2);

    // And: tokens contain correct org IDs
    assertThat(jwtUtil.getOrganizationIdFromClaims(jwtUtil.parseToken(token1))).isEqualTo(orgId1);
    assertThat(jwtUtil.getOrganizationIdFromClaims(jwtUtil.parseToken(token2))).isEqualTo(orgId2);
  }

  @Test
  @DisplayName("parseToken with valid token returns claims")
  void parseToken_WithValidToken_ReturnsClaims() {
    // Given: a valid token
    final String token = jwtUtil.generateToken(userId, organizationId, role);

    // When: token is parsed
    final Claims claims = jwtUtil.parseToken(token);

    // Then: claims are extracted correctly
    assertThat(claims).isNotNull();
    assertThat(claims.getSubject()).isEqualTo(userId.toString());
    assertThat(claims.get("organizationId", String.class)).isEqualTo(organizationId.toString());
    assertThat(claims.get("role", String.class)).isEqualTo(role);
  }

  @Test
  @DisplayName("parseToken with invalid signature throws JwtException")
  void parseToken_WithInvalidSignature_ThrowsException() {
    // Given: a token signed with a different secret
    final String invalidToken =
        JwtTestHelper.createInvalidSignatureToken(userId, organizationId, role);

    // When/Then: parsing throws JwtException
    assertThatThrownBy(() -> jwtUtil.parseToken(invalidToken)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("parseToken with expired token throws JwtException")
  void parseToken_WithExpiredToken_ThrowsException() {
    // Given: an expired token
    final String expiredToken = JwtTestHelper.createExpiredJwtToken(userId, organizationId, role);

    // When/Then: parsing throws JwtException (token is expired)
    // Note: We wait a bit to ensure token is expired
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    assertThatThrownBy(() -> jwtUtil.parseToken(expiredToken)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("parseToken with malformed token throws JwtException")
  void parseToken_WithMalformedToken_ThrowsException() {
    // Given: a malformed token
    final String malformedToken = "not.a.valid.token";

    // When/Then: parsing throws JwtException
    assertThatThrownBy(() -> jwtUtil.parseToken(malformedToken)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("getUserIdFromClaims with valid claims returns userId")
  void getUserIdFromClaims_WithValidClaims_ReturnsUserId() {
    // Given: valid token
    final String token = jwtUtil.generateToken(userId, organizationId, role);
    final Claims claims = jwtUtil.parseToken(token);

    // When: userId is extracted
    final UUID extractedUserId = jwtUtil.getUserIdFromClaims(claims);

    // Then: userId matches
    assertThat(extractedUserId).isEqualTo(userId);
  }

  @Test
  @DisplayName("getOrganizationIdFromClaims with valid claims returns organizationId")
  void getOrganizationIdFromClaims_WithValidClaims_ReturnsOrganizationId() {
    // Given: valid token
    final String token = jwtUtil.generateToken(userId, organizationId, role);
    final Claims claims = jwtUtil.parseToken(token);

    // When: organizationId is extracted
    final UUID extractedOrgId = jwtUtil.getOrganizationIdFromClaims(claims);

    // Then: organizationId matches
    assertThat(extractedOrgId).isEqualTo(organizationId);
  }

  @Test
  @DisplayName("getRoleFromClaims with valid claims returns role")
  void getRoleFromClaims_WithValidClaims_ReturnsRole() {
    // Given: valid token
    final String token = jwtUtil.generateToken(userId, organizationId, role);
    final Claims claims = jwtUtil.parseToken(token);

    // When: role is extracted
    final String extractedRole = jwtUtil.getRoleFromClaims(claims);

    // Then: role matches
    assertThat(extractedRole).isEqualTo(role);
  }

  @Test
  @DisplayName("isTokenExpired with expired token returns true")
  void isTokenExpired_WithExpiredToken_ReturnsTrue() {
    // Given: an expired token (created with very short expiration)
    final JwtUtil expiredJwtUtil = new JwtUtil(JwtTestHelper.getTestSecret(), 100L);
    final String expiredToken = expiredJwtUtil.generateToken(userId, organizationId, role);

    // Wait to ensure token is expired
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Parse the token (even though expired, we can still extract claims to check expiry)
    io.jsonwebtoken.ExpiredJwtException expiredException = null;
    Claims claims = null;
    try {
      claims = expiredJwtUtil.parseToken(expiredToken);
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      // Even when expired, we can extract claims from the exception
      expiredException = e;
      claims = e.getClaims();
    }

    assertThat(expiredException).isNotNull();

    // When: expiry is checked using jwtUtil (which uses different expiration time but same secret)
    // We use the claims from the expired token
    final boolean isExpired = jwtUtil.isTokenExpired(claims);

    // Then: token is expired
    assertThat(isExpired).isTrue();
  }

  @Test
  @DisplayName("isTokenExpired with valid token returns false")
  void isTokenExpired_WithValidToken_ReturnsFalse() {
    // Given: a valid token
    final String token = jwtUtil.generateToken(userId, organizationId, role);
    final Claims claims = jwtUtil.parseToken(token);

    // When: expiry is checked
    final boolean isExpired = jwtUtil.isTokenExpired(claims);

    // Then: token is not expired
    assertThat(isExpired).isFalse();
  }
}
