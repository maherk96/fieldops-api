package com.fieldops.fieldops_api.user.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.fieldops_api.config.SecurityConfig;
import com.fieldops.fieldops_api.security.JwtUtil;
import com.fieldops.fieldops_api.testutil.JwtTestHelper;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UpdateUserRequest;
import com.fieldops.fieldops_api.user.model.UserResponse;
import com.fieldops.fieldops_api.user.service.AdminUserService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for AdminUserResource.
 *
 * <p>Tests admin endpoints with MockMvc, including security and tenant isolation.
 */
@WebMvcTest(AdminUserResource.class)
@Import(SecurityConfig.class)
@DisplayName("AdminUserResource Tests")
class AdminUserResourceTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AdminUserService adminUserService;

  @MockBean private JwtUtil jwtUtil;

  @Autowired private ObjectMapper objectMapper;

  private UUID adminUserId;
  private UUID organizationId1;
  private UUID userId1;
  private UUID userId2;
  private String adminToken;
  private String engineerToken;

  @BeforeEach
  void setUp() {
    adminUserId = UUID.randomUUID();
    organizationId1 = UUID.randomUUID();
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();

    adminToken = JwtTestHelper.createJwtToken(adminUserId, organizationId1, "ADMIN");
    engineerToken = JwtTestHelper.createJwtToken(UUID.randomUUID(), organizationId1, "ENGINEER");

    // Setup JWT mock to parse tokens correctly
    setupJwtMockForToken(adminToken, adminUserId, organizationId1, "ADMIN");
    setupJwtMockForToken(engineerToken, UUID.randomUUID(), organizationId1, "ENGINEER");
  }

  private void setupJwtMockForToken(
      final String token, final UUID userId, final UUID organizationId, final String role) {
    final com.fieldops.fieldops_api.security.JwtUtil realJwtUtil =
        JwtTestHelper.createTestJwtUtil();
    try {
      final io.jsonwebtoken.Claims claims = realJwtUtil.parseToken(token);
      when(jwtUtil.parseToken(token)).thenReturn(claims);
      when(jwtUtil.isTokenExpired(claims)).thenReturn(false);
      when(jwtUtil.getUserIdFromClaims(claims)).thenReturn(userId);
      when(jwtUtil.getOrganizationIdFromClaims(claims)).thenReturn(organizationId);
      when(jwtUtil.getRoleFromClaims(claims)).thenReturn(role);
    } catch (final Exception e) {
      // If token parsing fails, mock will throw exception which is fine
      when(jwtUtil.parseToken(token)).thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));
    }
  }

  @Test
  @DisplayName("createUser without token returns 403")
  void createUser_WithoutToken_Returns403() throws Exception {
    // Given: create request without token
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    // When/Then: creation returns 403 (access denied without authentication)
    mockMvc
        .perform(
            post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("createUser with engineer role returns 403")
  void createUser_WithEngineerRole_Returns403() throws Exception {
    // Given: create request with engineer token
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    // When/Then: creation returns 403
    mockMvc
        .perform(
            post("/api/admin/users")
                .header("Authorization", "Bearer " + engineerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("createUser with admin role returns 201")
  void createUser_WithAdminRole_Returns201() throws Exception {
    // Given: create request with admin token
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    final UserResponse response =
        UserResponse.builder()
            .id(userId1)
            .email("newuser@example.com")
            .fullName("New User")
            .role("ENGINEER")
            .active(true)
            .organizationId(organizationId1)
            .dateCreated(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .build();

    when(adminUserService.createUser(any(CreateUserRequest.class))).thenReturn(response);

    // When/Then: creation returns 201
    mockMvc
        .perform(
            post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId1.toString()))
        .andExpect(jsonPath("$.email").value("newuser@example.com"))
        .andExpect(jsonPath("$.organizationId").value(organizationId1.toString()));
  }

  @Test
  @DisplayName("getAllUsers without token returns 403")
  void getAllUsers_WithoutToken_Returns403() throws Exception {
    // When/Then: retrieval returns 403 (access denied without authentication)
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getAllUsers with engineer role returns 403")
  void getAllUsers_WithEngineerRole_Returns403() throws Exception {
    // When/Then: retrieval returns 403
    mockMvc
        .perform(get("/api/admin/users").header("Authorization", "Bearer " + engineerToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getAllUsers with admin role returns 200")
  void getAllUsers_WithAdminRole_Returns200() throws Exception {
    // Given: users exist
    final UserResponse user1 =
        UserResponse.builder()
            .id(userId1)
            .email("user1@example.com")
            .fullName("User 1")
            .role("ENGINEER")
            .active(true)
            .organizationId(organizationId1)
            .dateCreated(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .build();

    final UserResponse user2 =
        UserResponse.builder()
            .id(userId2)
            .email("user2@example.com")
            .fullName("User 2")
            .role("ENGINEER")
            .active(true)
            .organizationId(organizationId1)
            .dateCreated(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .build();

    when(adminUserService.getAllUsers()).thenReturn(List.of(user1, user2));

    // When/Then: retrieval returns 200 with list
    mockMvc
        .perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(userId1.toString()))
        .andExpect(jsonPath("$[1].id").value(userId2.toString()));
  }

  @Test
  @DisplayName("getUser with admin role and same org returns 200")
  void getUser_WithAdminRoleAndSameOrg_Returns200() throws Exception {
    // Given: user exists in same org
    final UserResponse response =
        UserResponse.builder()
            .id(userId1)
            .email("user1@example.com")
            .fullName("User 1")
            .role("ENGINEER")
            .active(true)
            .organizationId(organizationId1)
            .dateCreated(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .build();

    when(adminUserService.getUser(eq(userId1))).thenReturn(response);

    // When/Then: retrieval returns 200
    mockMvc
        .perform(get("/api/admin/users/" + userId1).header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId1.toString()))
        .andExpect(jsonPath("$.organizationId").value(organizationId1.toString()));
  }

  @Test
  @DisplayName("getUser with admin role and different org returns 403")
  void getUser_WithAdminRoleAndDifferentOrg_Returns403() throws Exception {
    // Given: user exists in different org
    when(adminUserService.getUser(eq(userId2)))
        .thenThrow(
            new com.fieldops.fieldops_api.util.ForbiddenException(
                "Cannot access or modify users from other organizations"));

    // When/Then: retrieval returns 403
    mockMvc
        .perform(get("/api/admin/users/" + userId2).header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("updateUser with admin role and same org returns 200")
  void updateUser_WithAdminRoleAndSameOrg_Returns200() throws Exception {
    // Given: update request
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail("updated@example.com");
    request.setFullName("Updated Name");

    final UserResponse response =
        UserResponse.builder()
            .id(userId1)
            .email("updated@example.com")
            .fullName("Updated Name")
            .role("ENGINEER")
            .active(true)
            .organizationId(organizationId1)
            .dateCreated(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .build();

    when(adminUserService.updateUser(eq(userId1), any(UpdateUserRequest.class)))
        .thenReturn(response);

    // When/Then: update returns 200
    mockMvc
        .perform(
            put("/api/admin/users/" + userId1)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("updated@example.com"))
        .andExpect(jsonPath("$.fullName").value("Updated Name"));
  }

  @Test
  @DisplayName("updateUser with admin role and different org returns 403")
  void updateUser_WithAdminRoleAndDifferentOrg_Returns403() throws Exception {
    // Given: update request for user in different org
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail("updated@example.com");

    when(adminUserService.updateUser(eq(userId2), any(UpdateUserRequest.class)))
        .thenThrow(
            new com.fieldops.fieldops_api.util.ForbiddenException(
                "Cannot access or modify users from other organizations"));

    // When/Then: update returns 403
    mockMvc
        .perform(
            put("/api/admin/users/" + userId2)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("deactivateUser with admin role and same org returns 200")
  void deactivateUser_WithAdminRoleAndSameOrg_Returns200() throws Exception {
    // Given: user exists in same org
    final UserResponse response =
        UserResponse.builder()
            .id(userId1)
            .email("user1@example.com")
            .fullName("User 1")
            .role("ENGINEER")
            .active(false)
            .organizationId(organizationId1)
            .dateCreated(OffsetDateTime.now())
            .lastUpdated(OffsetDateTime.now())
            .build();

    when(adminUserService.deactivateUser(eq(userId1))).thenReturn(response);

    // When/Then: deactivation returns 200
    mockMvc
        .perform(
            patch("/api/admin/users/" + userId1 + "/deactivate")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  @Test
  @DisplayName("deactivateUser with admin role and different org returns 403")
  void deactivateUser_WithAdminRoleAndDifferentOrg_Returns403() throws Exception {
    // Given: user exists in different org
    when(adminUserService.deactivateUser(eq(userId2)))
        .thenThrow(
            new com.fieldops.fieldops_api.util.ForbiddenException(
                "Cannot access or modify users from other organizations"));

    // When/Then: deactivation returns 403
    mockMvc
        .perform(
            patch("/api/admin/users/" + userId2 + "/deactivate")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("all endpoints with invalid token returns 403")
  void allEndpoints_WithInvalidToken_Returns403() throws Exception {
    // Given: invalid token
    final String invalidToken = "invalid.token.here";

    // Mock JWT to throw exception for invalid token
    when(jwtUtil.parseToken(invalidToken))
        .thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

    // When/Then: all endpoints return 403 (access denied - invalid token means no authentication)
    mockMvc
        .perform(get("/api/admin/users").header("Authorization", "Bearer " + invalidToken))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/admin/users/" + userId1).header("Authorization", "Bearer " + invalidToken))
        .andExpect(status().isForbidden());
  }
}
