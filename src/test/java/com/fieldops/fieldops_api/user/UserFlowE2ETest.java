package com.fieldops.fieldops_api.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@Tag("e2e")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFlowE2ETest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate rest;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  @Test
  void admin_creates_user_user_logs_in_then_is_disabled() {
    // 1) Admin login (matches AdminUserSeeder)
    String adminToken = login("admin@fieldops.local", "admin123");

    // 2) Admin creates user
    CreateUserRequest create = new CreateUserRequest();
    create.setEmail("engineer1@test.com");
    create.setPassword("password123");
    create.setFullName("Engineer One");
    create.setRole("ENGINEER");
    create.setActive(true);

    ResponseEntity<UserDTO> createResp =
        rest.exchange(
            baseUrl() + "/api/users/admin/users",
            HttpMethod.POST,
            new HttpEntity<>(create, authHeaders(adminToken)),
            UserDTO.class);

    assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResp.getBody()).isNotNull();

    UUID userId = createResp.getBody().getId();
    assertThat(userId).isNotNull();

    // 3) User logs in
    String userToken = login("engineer1@test.com", "password123");

    // 4) User calls /me (works)
    ResponseEntity<UserDTO> meResp =
        rest.exchange(
            baseUrl() + "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(userToken)),
            UserDTO.class);

    assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meResp.getBody()).isNotNull();
    assertThat(meResp.getBody().getEmail()).isEqualTo("engineer1@test.com");
    assertThat(meResp.getBody().getActive()).isEqualTo(Boolean.TRUE);

    // 5) Admin disables user
    UserDTO update = new UserDTO();
    update.setActive(false);

    ResponseEntity<Void> disableResp =
        rest.exchange(
            baseUrl() + "/api/users/" + userId,
            HttpMethod.PUT,
            new HttpEntity<>(update, authHeaders(adminToken)),
            Void.class);

    assertThat(disableResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 6) Verify disabled (admin can fetch user and sees active=false)
    ResponseEntity<UserDTO> adminGetResp =
        rest.exchange(
            baseUrl() + "/api/users/" + userId,
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(adminToken)),
            UserDTO.class);

    assertThat(adminGetResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(adminGetResp.getBody()).isNotNull();
    assertThat(adminGetResp.getBody().getActive()).isEqualTo(Boolean.FALSE);

    // 7) Existing JWT may still work unless you enforce "active user" on every request
    ResponseEntity<UserDTO> stillWorksResp =
        rest.exchange(
            baseUrl() + "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(userToken)),
            UserDTO.class);

    // If you later implement "active check" per request, change this expected status to 401.
    assertThat(stillWorksResp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  /* ============================
   * Helpers
   * ============================ */

  private String login(String email, String password) {
    LoginRequest req = new LoginRequest();
    req.setEmail(email);
    req.setPassword(password);

    ResponseEntity<Map> resp = rest.postForEntity(baseUrl() + "/auth/login", req, Map.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody()).isNotNull();

    Object token = resp.getBody().get("accessToken");
    assertThat(token).isInstanceOf(String.class);
    assertThat((String) token).isNotBlank();

    return (String) token;
  }

  private HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
