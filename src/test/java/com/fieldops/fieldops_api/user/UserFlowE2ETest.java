package com.fieldops.fieldops_api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFlowE2ETest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private ObjectMapper objectMapper;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  @Test
  void admin_creates_user_user_logs_in_then_is_disabled() {
    // 1️⃣ Admin login
    String adminToken = login("admin@fieldops.com", "admin123");

    // 2️⃣ Admin creates user
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
    UUID userId = createResp.getBody().getId();

    // 3️⃣ User logs in
    String userToken = login("engineer1@test.com", "password123");

    // 4️⃣ User calls /me
    ResponseEntity<UserDTO> meResp =
        rest.exchange(
            baseUrl() + "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(userToken)),
            UserDTO.class);

    assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meResp.getBody().getEmail()).isEqualTo("engineer1@test.com");

    // 5️⃣ Admin disables user
    UserDTO update = new UserDTO();
    update.setActive(false);

    ResponseEntity<Void> disableResp =
        rest.exchange(
            baseUrl() + "/api/users/" + userId,
            HttpMethod.PUT,
            new HttpEntity<>(update, authHeaders(adminToken)),
            Void.class);

    assertThat(disableResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 6️⃣ User is blocked
    ResponseEntity<String> blockedResp =
        rest.exchange(
            baseUrl() + "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders(userToken)),
            String.class);

    assertThat(blockedResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  /* ============================
   * Helpers
   * ============================ */

  private String login(String email, String password) {
    LoginRequest req = new LoginRequest();
    req.setEmail(email);
    req.setPassword(password);

    ResponseEntity<Map> resp =
        rest.postForEntity(
            baseUrl() + "/auth/login",
            req,
            Map.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    return (String) resp.getBody().get("accessToken");
  }

  private HttpHeaders authHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}