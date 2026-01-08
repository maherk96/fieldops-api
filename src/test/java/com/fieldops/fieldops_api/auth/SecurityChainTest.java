package com.fieldops.fieldops_api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SecurityChainTest extends AbstractAuthIntegrationTest {

  @Test
  void login_is_public() throws Exception {
    // Missing fields should yield 400 (validation), not 401
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void auth_me_is_protected() throws Exception {
    mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void random_endpoint_is_protected_by_default() throws Exception {
    mockMvc.perform(get("/api/work-orders/all")).andExpect(status().isUnauthorized());
  }
}
