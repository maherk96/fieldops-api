package com.fieldops.fieldops_api.auth.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.auth.model.LoginResponse;
import com.fieldops.fieldops_api.auth.service.AuthService;
import com.fieldops.fieldops_api.config.SecurityConfig;
import com.fieldops.fieldops_api.security.JwtUtil;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for AuthController.
 *
 * <p>Tests login endpoint with MockMvc, including validation and security.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthService authService;

  @MockBean private JwtUtil jwtUtil;

  @Autowired private ObjectMapper objectMapper;

  private UUID userId;
  private UUID organizationId;
  private String email;
  private String password;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    organizationId = UUID.randomUUID();
    email = "test@example.com";
    password = "password123";
  }

  @Test
  @DisplayName("login with valid subdomain request returns 200 with token")
  void login_WithValidSubdomainRequest_Returns200WithToken() throws Exception {
    // Given: valid login request with subdomain
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain("testorg");

    final LoginResponse response =
        new LoginResponse("test-token", userId, email, "ADMIN", organizationId);

    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    // When/Then: login returns 200 with token
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("test-token"))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.role").value("ADMIN"))
        .andExpect(jsonPath("$.organizationId").value(organizationId.toString()));
  }

  @Test
  @DisplayName("login with valid orgId request returns 200 with token")
  void login_WithValidOrgIdRequest_Returns200WithToken() throws Exception {
    // Given: valid login request with organizationId
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setOrganizationId(organizationId.toString());

    final LoginResponse response =
        new LoginResponse("test-token", userId, email, "ENGINEER", organizationId);

    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    // When/Then: login returns 200 with token
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("test-token"))
        .andExpect(jsonPath("$.role").value("ENGINEER"));
  }

  @Test
  @DisplayName("login with invalid credentials returns 401")
  void login_WithInvalidCredentials_Returns401() throws Exception {
    // Given: login request with invalid credentials
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword("wrongpassword");
    request.setSubdomain("testorg");

    when(authService.login(any(LoginRequest.class)))
        .thenThrow(
            new org.springframework.security.authentication.BadCredentialsException(
                "Invalid credentials"));

    // When/Then: login returns 401
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("login with missing email returns 400")
  void login_WithMissingEmail_Returns400() throws Exception {
    // Given: login request without email
    final LoginRequest request = new LoginRequest();
    request.setPassword(password);
    request.setSubdomain("testorg");

    // When/Then: login returns 400 (validation error)
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("login with missing password returns 400")
  void login_WithMissingPassword_Returns400() throws Exception {
    // Given: login request without password
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setSubdomain("testorg");

    // When/Then: login returns 400 (validation error)
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("login with invalid email format returns 400")
  void login_WithInvalidEmailFormat_Returns400() throws Exception {
    // Given: login request with invalid email
    final LoginRequest request = new LoginRequest();
    request.setEmail("invalid-email");
    request.setPassword(password);
    request.setSubdomain("testorg");

    // When/Then: login returns 400 (validation error)
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("login with both subdomain and orgId returns 400")
  void login_WithBothSubdomainAndOrgId_Returns400() throws Exception {
    // Given: login request with both subdomain and orgId
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain("testorg");
    request.setOrganizationId(organizationId.toString());

    when(authService.login(any(LoginRequest.class)))
        .thenThrow(
            new IllegalArgumentException(
                "Only one of subdomain or organizationId should be provided"));

    // When/Then: login returns 400
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("login with neither subdomain nor orgId returns 400")
  void login_WithNeitherSubdomainNorOrgId_Returns400() throws Exception {
    // Given: login request without subdomain or orgId
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);

    when(authService.login(any(LoginRequest.class)))
        .thenThrow(
            new IllegalArgumentException("Either subdomain or organizationId must be provided"));

    // When/Then: login returns 400
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("login with inactive user returns 401")
  void login_WithInactiveUser_Returns401() throws Exception {
    // Given: login request for inactive user
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain("testorg");

    when(authService.login(any(LoginRequest.class)))
        .thenThrow(
            new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "User is inactive"));

    // When/Then: login returns 401
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
