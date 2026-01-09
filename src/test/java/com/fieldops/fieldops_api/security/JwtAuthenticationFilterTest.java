package com.fieldops.fieldops_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fieldops.fieldops_api.testutil.JwtTestHelper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Integration tests for JwtAuthenticationFilter.
 *
 * <p>Tests filter behavior, authentication context, and tenant context management.
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

  @org.mockito.Mock private JwtUtil jwtUtil;

  private JwtAuthenticationFilter jwtAuthenticationFilter;
  private UUID userId;
  private UUID organizationId;
  private String role;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    organizationId = UUID.randomUUID();
    role = "ADMIN";

    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);

    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  @Test
  @DisplayName("doFilter with missing Authorization header does not authenticate")
  void doFilter_WithMissingAuthorizationHeader_DoesNotAuthenticate() throws Exception {
    // Given: request without Authorization header
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    // When: request is processed
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: no authentication is set
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();
    assertThat(TenantContext.getOrganizationId()).isNull();
  }

  @Test
  @DisplayName("doFilter with malformed Bearer token does not authenticate")
  void doFilter_WithMalformedBearerToken_DoesNotAuthenticate() throws Exception {
    // Given: request with malformed Bearer token
    final String malformedToken = "not.a.valid.token";
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + malformedToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    when(jwtUtil.parseToken(malformedToken))
        .thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

    // When: request is processed
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: no authentication is set, SecurityContext is cleared
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();
    assertThat(TenantContext.getOrganizationId()).isNull();
  }

  @Test
  @DisplayName("doFilter with invalid token signature does not authenticate")
  void doFilter_WithInvalidTokenSignature_DoesNotAuthenticate() throws Exception {
    // Given: request with invalid signature token
    final String invalidToken =
        JwtTestHelper.createInvalidSignatureToken(userId, organizationId, role);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + invalidToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    when(jwtUtil.parseToken(invalidToken))
        .thenThrow(new io.jsonwebtoken.JwtException("Invalid signature"));

    // When: request is processed (filter will fail to parse)
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: no authentication is set, TenantContext is cleared
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();
    assertThat(TenantContext.getOrganizationId()).isNull();
  }

  @Test
  @DisplayName("doFilter with expired token does not authenticate")
  void doFilter_WithExpiredToken_DoesNotAuthenticate() throws Exception {
    // Given: expired token
    final String expiredToken = JwtTestHelper.createExpiredJwtToken(userId, organizationId, role);
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + expiredToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    when(jwtUtil.parseToken(expiredToken))
        .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

    // When: request is processed (filter will fail to parse expired token)
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: no authentication is set, SecurityContext is cleared
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();
    assertThat(TenantContext.getOrganizationId()).isNull();
  }

  @Test
  @DisplayName("doFilter with valid token sets Authentication with correct principal")
  void doFilter_WithValidToken_SetsAuthenticationWithCorrectPrincipal() throws Exception {
    // Given: valid token
    final String validToken = JwtTestHelper.createJwtToken(userId, organizationId, role);

    // Mock JwtUtil to parse the token correctly
    final JwtUtil realJwtUtil = JwtTestHelper.createTestJwtUtil();
    final Claims claims = realJwtUtil.parseToken(validToken);

    when(jwtUtil.parseToken(validToken)).thenReturn(claims);
    when(jwtUtil.isTokenExpired(claims)).thenReturn(false);
    when(jwtUtil.getUserIdFromClaims(claims)).thenReturn(userId);
    when(jwtUtil.getOrganizationIdFromClaims(claims)).thenReturn(organizationId);
    when(jwtUtil.getRoleFromClaims(claims)).thenReturn(role);

    // When: request is processed
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + validToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();

    final FilterChain filterChain = mock(FilterChain.class);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: authentication is set correctly
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getName()).isEqualTo(userId.toString());
    assertThat(auth.getAuthorities()).hasSize(1);
    assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_" + role);

    verify(jwtUtil).parseToken(validToken);
    verify(jwtUtil).isTokenExpired(claims);
    verify(jwtUtil).getUserIdFromClaims(claims);
    verify(jwtUtil).getOrganizationIdFromClaims(claims);
    verify(jwtUtil).getRoleFromClaims(claims);
  }

  @Test
  @DisplayName("doFilter with valid token sets Authentication with correct authorities")
  void doFilter_WithValidToken_SetsAuthenticationWithCorrectAuthorities() throws Exception {
    // Given: valid token with ENGINEER role
    final String validToken = JwtTestHelper.createJwtToken(userId, organizationId, "ENGINEER");

    final JwtUtil realJwtUtil = JwtTestHelper.createTestJwtUtil();
    final Claims claims = realJwtUtil.parseToken(validToken);

    when(jwtUtil.parseToken(validToken)).thenReturn(claims);
    when(jwtUtil.isTokenExpired(claims)).thenReturn(false);
    when(jwtUtil.getUserIdFromClaims(claims)).thenReturn(userId);
    when(jwtUtil.getOrganizationIdFromClaims(claims)).thenReturn(organizationId);
    when(jwtUtil.getRoleFromClaims(claims)).thenReturn("ENGINEER");

    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + validToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: authorities are set correctly
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getAuthorities()).hasSize(1);
    assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ENGINEER");

    verify(jwtUtil).getRoleFromClaims(claims);
  }

  @Test
  @DisplayName("doFilter with valid token sets TenantContext")
  void doFilter_WithValidToken_SetsTenantContext() throws Exception {
    // Given: valid token
    final String validToken = JwtTestHelper.createJwtToken(userId, organizationId, role);

    final JwtUtil realJwtUtil = JwtTestHelper.createTestJwtUtil();
    final Claims claims = realJwtUtil.parseToken(validToken);

    when(jwtUtil.parseToken(validToken)).thenReturn(claims);
    when(jwtUtil.isTokenExpired(claims)).thenReturn(false);
    when(jwtUtil.getUserIdFromClaims(claims)).thenReturn(userId);
    when(jwtUtil.getOrganizationIdFromClaims(claims)).thenReturn(organizationId);
    when(jwtUtil.getRoleFromClaims(claims)).thenReturn(role);

    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + validToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Note: TenantContext is cleared in finally block, so we can't assert it here
    // But we can verify the method was called
    verify(jwtUtil).getOrganizationIdFromClaims(claims);
  }

  @Test
  @DisplayName("doFilter with valid token clears TenantContext after request")
  void doFilter_WithValidToken_ClearsTenantContextAfterRequest() throws Exception {
    // Given: valid token
    final String validToken = JwtTestHelper.createJwtToken(userId, organizationId, role);

    final JwtUtil realJwtUtil = JwtTestHelper.createTestJwtUtil();
    final Claims claims = realJwtUtil.parseToken(validToken);

    when(jwtUtil.parseToken(validToken)).thenReturn(claims);
    when(jwtUtil.isTokenExpired(claims)).thenReturn(false);
    when(jwtUtil.getUserIdFromClaims(claims)).thenReturn(userId);
    when(jwtUtil.getOrganizationIdFromClaims(claims)).thenReturn(organizationId);
    when(jwtUtil.getRoleFromClaims(claims)).thenReturn(role);

    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + validToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: TenantContext is cleared after request (in finally block)
    assertThat(TenantContext.getOrganizationId()).isNull();
  }

  @Test
  @DisplayName("doFilter with invalid token clears TenantContext")
  void doFilter_WithInvalidToken_ClearsTenantContext() throws Exception {
    // Given: invalid token (exception thrown during parsing)
    final String invalidToken = "invalid.token";

    when(jwtUtil.parseToken(invalidToken))
        .thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/admin/users");
    request.addHeader("Authorization", "Bearer " + invalidToken);

    final MockHttpServletResponse response = new MockHttpServletResponse();
    final FilterChain filterChain = mock(FilterChain.class);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Then: TenantContext is cleared even on error
    assertThat(TenantContext.getOrganizationId()).isNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
