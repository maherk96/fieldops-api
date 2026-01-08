package com.fieldops.fieldops_api.user.rest;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.service.UserManagementService;
import com.fieldops.fieldops_api.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserResource.class)
@Import(UserResourceTest.TestConfig.class)
@EnableMethodSecurity
class UserResourceTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserService userService;
  @Autowired private UserManagementService userManagementService;
  @Autowired private ObjectMapper objectMapper;

  /* ----------------------------
   * GET /api/users (ADMIN)
   * ---------------------------- */

  @Test
  void admin_can_list_users() throws Exception {
    UserDTO user = new UserDTO();
    user.setId(UUID.randomUUID());
    user.setEmail("admin@test.com");

    when(userService.findAll()).thenReturn(List.of(user));

    mockMvc
        .perform(get("/api/users").with(authentication(adminAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("admin@test.com"));
  }

  @Test
  @WithAnonymousUser
  void anonymous_cannot_list_users() throws Exception {
    mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
  }

  @Test
  void non_admin_cannot_list_users() throws Exception {
    mockMvc
        .perform(get("/api/users").with(authentication(userAuth(UUID.randomUUID()))))
        .andExpect(status().isForbidden());
  }

  /* ----------------------------
   * GET /api/users/{id}
   * ---------------------------- */

  @Test
  void user_can_get_self() throws Exception {
    UUID userId = UUID.randomUUID();

    UserDTO dto = new UserDTO();
    dto.setId(userId);
    dto.setEmail("self@test.com");

    when(userService.get(userId)).thenReturn(dto);

    mockMvc
        .perform(get("/api/users/{id}", userId).with(authentication(userAuth(userId))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("self@test.com"));
  }

  @Test
  void user_cannot_get_other_user() throws Exception {
    mockMvc
        .perform(
            get("/api/users/{id}", UUID.randomUUID())
                .with(authentication(userAuth(UUID.randomUUID()))))
        .andExpect(status().isForbidden());
  }

  /* ----------------------------
   * POST /api/users/admin/users
   * ---------------------------- */

  @Test
  void admin_can_create_user() throws Exception {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("new@test.com");
    request.setPassword("password");
    request.setRole("ENGINEER");

    UserDTO created = new UserDTO();
    created.setId(UUID.randomUUID());
    created.setEmail("new@test.com");

    when(userManagementService.createUserWithPassword(any())).thenReturn(created);

    mockMvc
        .perform(
            post("/api/users/admin/users")
                .with(authentication(adminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("new@test.com"));
  }

  /* ----------------------------
   * PUT /api/users/{id}
   * ---------------------------- */

  @Test
  void self_can_update_self() throws Exception {
    UUID userId = UUID.randomUUID();

    UserDTO update = new UserDTO();
    update.setFullName("Updated Name");

    mockMvc
        .perform(
            put("/api/users/{id}", userId)
                .with(authentication(userAuth(userId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
        .andExpect(status().isOk());

    verify(userManagementService).updateUser(eq(userId), any());
  }

  /* ----------------------------
   * DELETE /api/users/{id}
   * ---------------------------- */

  @Test
  void admin_can_delete_user() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/users/{id}", id).with(authentication(adminAuth())))
        .andExpect(status().isNoContent());

    verify(userService).delete(id);
  }

  /* ----------------------------
   * GET /api/users/me
   * ---------------------------- */

  @Test
  void authenticated_user_can_get_me() throws Exception {
    UUID userId = UUID.randomUUID();

    UserDTO dto = new UserDTO();
    dto.setId(userId);
    dto.setEmail("me@test.com");

    when(userService.get(userId)).thenReturn(dto);

    mockMvc
        .perform(get("/api/users/me").with(authentication(userAuth(userId))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("me@test.com"));
  }

  /* ============================
   * Helpers
   * ============================ */

  private static UsernamePasswordAuthenticationToken adminAuth() {
    return new UsernamePasswordAuthenticationToken(
        UUID.randomUUID(), null, List.of(() -> "ROLE_ADMIN"));
  }

  private static UsernamePasswordAuthenticationToken userAuth(UUID userId) {
    return new UsernamePasswordAuthenticationToken(userId, null, List.of(() -> "ROLE_ENGINEER"));
  }

  /* ============================
   * Test Configuration
   * ============================ */

  @TestConfiguration
  static class TestConfig {

    @Bean
    UserService userService() {
      return mock(UserService.class);
    }

    @Bean
    UserManagementService userManagementService() {
      return mock(UserManagementService.class);
    }
  }
}
