package com.fieldops.fieldops_api.auth.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

  private String accessToken;
  private long expiresIn;
  private UserInfo user;

  @Getter
  @Setter
  @AllArgsConstructor
  public static class UserInfo {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private Boolean active;
  }
}
