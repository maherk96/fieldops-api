package com.fieldops.fieldops_api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter responsible for authenticating requests using JWT tokens.
 *
 * <p>The filter:
 *
 * <ul>
 *   <li>Extracts the JWT from the {@code Authorization} header
 *   <li>Validates the token
 *   <li>Populates the {@link SecurityContextHolder} if authentication succeeds
 * </ul>
 *
 * <p>If the token is missing, invalid, or malformed, the request is rejected with {@code 401
 * Unauthorized}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtService jwtService;

  /**
   * Creates a new JWT authentication filter.
   *
   * @param jwtService service used for JWT validation and claim extraction
   */
  public JwtAuthenticationFilter(final JwtService jwtService) {
    this.jwtService = jwtService;
  }

  /**
   * Filters incoming HTTP requests and performs JWT-based authentication.
   *
   * <p>If a valid JWT is found, an authenticated {@link UsernamePasswordAuthenticationToken} is
   * created using the user ID as the principal and the extracted role as a granted authority.
   *
   * @param request the incoming HTTP request
   * @param response the HTTP response
   * @param filterChain the remaining filter chain
   * @throws ServletException if the filter fails
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final var authHeader = request.getHeader("Authorization");

    // No JWT present – continue without authentication
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final var jwt = authHeader.substring(7);

      if (!jwtService.isTokenValid(jwt)) {
        // Invalid token: fail fast
        if (log.isDebugEnabled()) {
          log.debug("Invalid JWT presented for URI {}", request.getRequestURI());
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      final UUID userId = jwtService.extractUserId(jwt);
      final String role = jwtService.extractRole(jwt);

      // Map role claim to Spring Security authority
      final List<SimpleGrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_" + role));

      var authToken = new UsernamePasswordAuthenticationToken(userId, null, authorities);

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authToken);

    } catch (Exception e) {
      // Malformed or tampered token
      if (log.isDebugEnabled()) {
        log.debug("JWT processing failed for URI {}: {}", request.getRequestURI(), e.getMessage());
      }
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
