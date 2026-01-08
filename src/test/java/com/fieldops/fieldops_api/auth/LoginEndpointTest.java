package com.fieldops.fieldops_api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class LoginEndpointTest extends AbstractAuthIntegrationTest {

  private static final Pattern UUID_V4 =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

  @Test
  void login_success_returns_token_and_user() throws Exception {
    createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);

    var result =
        mockMvc
            .perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "email", "engineer@example.com",
                                "password", "password123"))))
            .andExpect(status().isOk())
            .andReturn();

    // No session cookie
    List<String> setCookies = result.getResponse().getHeaders("Set-Cookie");
    assertThat(setCookies).isEmpty();

    Map<String, Object> body =
        objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
    assertThat(body.get("accessToken")).isInstanceOf(String.class);
    assertThat(((String) body.get("accessToken")).length()).isGreaterThan(10);

    assertThat(body.get("expiresIn")).isInstanceOf(Number.class);
    assertThat(((Number) body.get("expiresIn")).longValue()).isGreaterThan(0L);

    @SuppressWarnings("unchecked")
    Map<String, Object> user = (Map<String, Object>) body.get("user");
    assertThat(user.get("email")).isEqualTo("engineer@example.com");
    assertThat(user.get("role")).isEqualTo("ENGINEER");
    assertThat(user.get("active")).isEqualTo(Boolean.TRUE);
    assertThat(user).doesNotContainKey("password");
    assertThat(user.get("id")).isInstanceOf(String.class);
    assertThat(UUID_V4.matcher((String) user.get("id")).matches()).isTrue();
  }

  @Test
  void login_wrong_password_401() throws Exception {
    createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", true);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", "engineer@example.com",
                            "password", "wrong"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_unknown_email_401() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", "nobody@example.com",
                            "password", "password123"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_inactive_user_401() throws Exception {
    createUser("engineer@example.com", "password123", "John Engineer", "ENGINEER", false);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", "engineer@example.com",
                            "password", "password123"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_missing_fields_400() throws Exception {
    // Missing email
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("password", "password123"))))
        .andExpect(status().isBadRequest());

    // Missing password
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "engineer@example.com"))))
        .andExpect(status().isBadRequest());

    // Invalid email
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "email", "not-an-email",
                            "password", "password123"))))
        .andExpect(status().isBadRequest());

    // Empty password
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"engineer@example.com\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }
}
