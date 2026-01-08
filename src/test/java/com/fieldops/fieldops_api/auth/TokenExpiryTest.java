package com.fieldops.fieldops_api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

@SpringBootTest(
    properties = {
      "jwt.expiration=1000" // 1 second
    })
class TokenExpiryTest extends AbstractAuthIntegrationTest {

  @Test
  void expired_token_returns_401() throws Exception {
    createUser("exp@example.com", "password123", "Exp User", "ENGINEER", true);
    String token = loginAndGetToken("exp@example.com", "password123");

    Thread.sleep(1200); // wait > 1 second

    mockMvc
        .perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }
}
