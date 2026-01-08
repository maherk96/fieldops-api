package com.fieldops.fieldops_api.auth;

import com.fieldops.fieldops_api.auth.model.CurrentUserResponse;
import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.auth.model.LoginResponse;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthService(
      final UserRepository userRepository,
      final JwtService jwtService,
      final AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  public LoginResponse login(LoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid email or password");
    }

    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    if (!user.getActive()) {
      throw new BadCredentialsException("User account is inactive");
    }

    String token = jwtService.generateToken(user.getId(), user.getRole(), user.getEmail());

    LoginResponse.UserInfo userInfo =
        new LoginResponse.UserInfo(
            user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getActive());

    return new LoginResponse(token, jwtService.getExpirationInSeconds(), userInfo);
  }

  public CurrentUserResponse getCurrentUser(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new org.springframework.security.authentication.BadCredentialsException(
                        "Invalid token or user not found"));

    if (!user.getActive()) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "User account is inactive");
    }

    return new CurrentUserResponse(
        user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getActive());
  }
}
