package com.fieldops.fieldops_api.auth;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityExceptionHandler {

  @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
  public ResponseEntity<Map<String, Object>> handleAuthErrors(RuntimeException ex) {
    return ResponseEntity.status(401).body(Map.of("status", 401, "message", ex.getMessage()));
  }
}
