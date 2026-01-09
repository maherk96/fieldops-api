package com.fieldops.fieldops_api.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for security-related exceptions.
 *
 * <p>Handles:
 *
 * <ul>
 *   <li>Invalid credentials (401 Unauthorized)
 *   <li>Authentication exceptions (401 Unauthorized)
 *   <li>Cross-tenant access attempts (handled by ForbiddenException -> 403)
 * </ul>
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

  /**
   * Handles invalid credentials exceptions.
   *
   * <p>Returns 401 Unauthorized with a clear error message.
   *
   * @param ex the bad credentials exception
   * @return error response
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(final BadCredentialsException ex) {
    final ErrorResponse error = new ErrorResponse("Invalid credentials", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handles general authentication exceptions.
   *
   * <p>Returns 401 Unauthorized with a clear error message.
   *
   * @param ex the authentication exception
   * @return error response
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      final AuthenticationException ex) {
    final ErrorResponse error = new ErrorResponse("Authentication failed", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handles illegal argument exceptions.
   *
   * <p>Returns 400 Bad Request with a clear error message.
   *
   * @param ex the illegal argument exception
   * @return error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      final IllegalArgumentException ex) {
    final ErrorResponse error = new ErrorResponse("Invalid request", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Error response DTO for security exceptions.
   *
   * <p>Provides a clean JSON structure for error responses.
   */
  public static class ErrorResponse {
    private final String error;
    private final String message;

    public ErrorResponse(final String error, final String message) {
      this.error = error;
      this.message = message;
    }

    public String getError() {
      return error;
    }

    public String getMessage() {
      return message;
    }
  }
}
