package com.fieldops.fieldops_api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that extracts and validates JWT tokens from requests.
 *
 * <p>This filter:
 *
 * <ul>
 *   <li>Extracts JWT tokens from the Authorization header (Bearer token)
 *   <li>Validates and parses the token
 *   <li>Extracts organizationId, userId, and role from token claims
 *   <li>Sets the authentication context and tenant context
 *   <li>Clears tenant context after request processing
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;

  public JwtAuthenticationFilter(final JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      @NonNull final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain filterChain)
      throws ServletException, IOException {

    try {
      final String token = extractToken(request);

      if (token != null) {
        try {
          final Claims claims = jwtUtil.parseToken(token);

          if (!jwtUtil.isTokenExpired(claims)) {
            final UUID userId = jwtUtil.getUserIdFromClaims(claims);
            final UUID organizationId = jwtUtil.getOrganizationIdFromClaims(claims);
            final String role = jwtUtil.getRoleFromClaims(claims);

            // Set tenant context
            TenantContext.setOrganizationId(organizationId);

            // Create authentication token
            final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userId.toString(), // principal (user ID)
                    null, // credentials
                    java.util.Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        } catch (final Exception e) {
          // Invalid token - clear context and continue (will be handled by security chain)
          SecurityContextHolder.clearContext();
          TenantContext.clear();
        }
      }

      filterChain.doFilter(request, response);
    } finally {
      // Always clear tenant context after request processing to prevent memory leaks
      TenantContext.clear();
    }
  }

  /**
   * Extracts the JWT token from the Authorization header.
   *
   * @param request the HTTP request
   * @return the token string, or null if not found
   */
  private String extractToken(final HttpServletRequest request) {
    final String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
