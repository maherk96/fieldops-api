package com.fieldops.fieldops_api.auth;

import com.fieldops.fieldops_api.auth.model.CurrentUserResponse;
import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.auth.model.LoginResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(final AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<CurrentUserResponse> getCurrentUser(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof UUID)) {
      return ResponseEntity.status(401).build();
    }

    UUID userId = (UUID) authentication.getPrincipal();
    CurrentUserResponse response = authService.getCurrentUser(userId);
    return ResponseEntity.ok(response);
  }
}
