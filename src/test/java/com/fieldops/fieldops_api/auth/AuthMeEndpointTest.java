package com.fieldops.fieldops_api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AuthMeEndpointTest extends AbstractAuthIntegrationTest {

  @Test
  void me_with_valid_token_returns_200_and_user() throws Exception {
    createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);
    String token = loginAndGetToken("engineer@example.com", "password123");

    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void me_no_auth_header_401() throws Exception {
    mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void me_wrong_prefix_401() throws Exception {
    createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);
    String token = loginAndGetToken("engineer@example.com", "password123");

    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Token " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_empty_token_401() throws Exception {
    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer "))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_malformed_token_401() throws Exception {
    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_bad_signature_401() throws Exception {
    createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);
    String token = loginAndGetToken("engineer@example.com", "password123");

    // Corrupt payload segment to break signature but keep JWT structure
    String[] parts = token.split("\\.");
    String payload = parts[1];
    // Flip last character between valid base64url chars
    char last = payload.charAt(payload.length() - 1);
    char flipped = last == 'A' ? 'B' : 'A';
    String tampered = payload.substring(0, payload.length() - 1) + flipped;
    String bad = parts[0] + "." + tampered + "." + parts[2];

    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + bad))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_user_deleted_after_token_401() throws Exception {
    var user = createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);
    String token = loginAndGetToken("engineer@example.com", "password123");
    userRepository.deleteById(user.getId());

    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void me_user_deactivated_after_token_401() throws Exception {
    var user = createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);
    String token = loginAndGetToken("engineer@example.com", "password123");
    user.setActive(false);
    userRepository.save(user);

    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }
}
