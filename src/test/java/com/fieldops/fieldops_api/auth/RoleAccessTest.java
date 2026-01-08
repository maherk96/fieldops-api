package com.fieldops.fieldops_api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class RoleAccessTest extends AbstractAuthIntegrationTest {

  @Test
  void engineer_denied_dispatcher_admin_endpoint() throws Exception {
    createUser("eng@example.com", "password123", "Eng", "ENGINEER", true);
    String token = loginAndGetToken("eng@example.com", "password123");
    mockMvc
        .perform(get("/api/work-orders/all").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void dispatcher_allowed_dispatcher_admin_endpoint() throws Exception {
    createUser("disp@example.com", "password123", "Disp", "DISPATCHER", true);
    String token = loginAndGetToken("disp@example.com", "password123");
    mockMvc
        .perform(get("/api/work-orders/all").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void dispatcher_denied_admin_only_endpoint() throws Exception {
    createUser("disp@example.com", "password123", "Disp", "DISPATCHER", true);
    String token = loginAndGetToken("disp@example.com", "password123");
    mockMvc
        .perform(
            get("/api/work-orders/admin-only").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void admin_allowed_admin_only_endpoint() throws Exception {
    createUser("admin@example.com", "password123", "Admin", "ADMIN", true);
    String token = loginAndGetToken("admin@example.com", "password123");
    mockMvc
        .perform(
            get("/api/work-orders/admin-only").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());
  }
}
