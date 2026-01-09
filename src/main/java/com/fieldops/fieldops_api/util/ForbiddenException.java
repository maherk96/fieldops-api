package com.fieldops.fieldops_api.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when access is forbidden (403).
 *
 * <p>Used for:
 *
 * <ul>
 *   <li>Cross-tenant access attempts
 *   <li>Unauthorized access to resources
 * </ul>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

  public ForbiddenException(final String message) {
    super(message);
  }

  public ForbiddenException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
