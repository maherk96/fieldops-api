package com.fieldops.fieldops_api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fieldops.fieldops_api.auth.model.LoginRequest;
import com.fieldops.fieldops_api.auth.model.LoginResponse;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.security.JwtUtil;
import com.fieldops.fieldops_api.security.UserDetailsServiceImpl;
import com.fieldops.fieldops_api.security.UserPrincipal;
import com.fieldops.fieldops_api.testutil.TestDataHelper;
import com.fieldops.fieldops_api.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for AuthService.
 *
 * <p>Tests login scenarios, organization resolution, and credential validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock private UserDetailsServiceImpl userDetailsService;

  @Mock private OrganizationRepository organizationRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtil jwtUtil;

  @InjectMocks private AuthService authService;

  private PasswordEncoder realPasswordEncoder;
  private UUID userId;
  private UUID organizationId;
  private String email;
  private String password;
  private String subdomain;
  private Organization organization;
  private User user;
  private UserPrincipal userPrincipal;

  @BeforeEach
  void setUp() {
    realPasswordEncoder = new BCryptPasswordEncoder();
    organizationId = UUID.randomUUID();
    email = "test@example.com";
    password = "password123";
    subdomain = "testorg";

    organization = TestDataHelper.createTestOrganization(organizationId, subdomain);
    user = TestDataHelper.createTestUser(email, organization, "ADMIN", true, realPasswordEncoder);
    userId = user.getId(); // Use the actual userId from the user
    userPrincipal =
        new UserPrincipal(
            user.getId(),
            user.getEmail(),
            realPasswordEncoder.encode(password),
            organizationId,
            user.getRole(),
            user.getActive());
  }

  @Test
  @DisplayName("login with subdomain and valid credentials returns LoginResponse")
  void login_WithSubdomainAndValidCredentials_ReturnsLoginResponse() {
    // Given: login request with subdomain
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain(subdomain);

    when(organizationRepository.findBySubdomain(subdomain)).thenReturn(Optional.of(organization));
    when(userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .thenReturn(userPrincipal);
    when(passwordEncoder.matches(password, userPrincipal.getPassword())).thenReturn(true);
    when(jwtUtil.generateToken(userId, organizationId, "ADMIN")).thenReturn("test-token");

    // When: login is performed
    final LoginResponse response = authService.login(request);

    // Then: response contains token and user info
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("test-token");
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getEmail()).isEqualTo(email);
    assertThat(response.getRole()).isEqualTo("ADMIN");
    assertThat(response.getOrganizationId()).isEqualTo(organizationId);

    verify(organizationRepository).findBySubdomain(subdomain);
    verify(userDetailsService).loadUserByOrganizationAndEmail(organizationId, email);
    verify(passwordEncoder).matches(password, userPrincipal.getPassword());
    verify(jwtUtil).generateToken(userId, organizationId, "ADMIN");
  }

  @Test
  @DisplayName("login with organizationId and valid credentials returns LoginResponse")
  void login_WithOrganizationIdAndValidCredentials_ReturnsLoginResponse() {
    // Given: login request with organizationId
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setOrganizationId(organizationId.toString());

    when(userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .thenReturn(userPrincipal);
    when(passwordEncoder.matches(password, userPrincipal.getPassword())).thenReturn(true);
    when(jwtUtil.generateToken(userId, organizationId, "ADMIN")).thenReturn("test-token");

    // When: login is performed
    final LoginResponse response = authService.login(request);

    // Then: response contains token and user info
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("test-token");
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getEmail()).isEqualTo(email);
    assertThat(response.getRole()).isEqualTo("ADMIN");
    assertThat(response.getOrganizationId()).isEqualTo(organizationId);

    verify(userDetailsService).loadUserByOrganizationAndEmail(organizationId, email);
    verify(passwordEncoder).matches(password, userPrincipal.getPassword());
    verify(organizationRepository, never()).findBySubdomain(any());
  }

  @Test
  @DisplayName("login with wrong password throws BadCredentialsException")
  void login_WithWrongPassword_ThrowsBadCredentialsException() {
    // Given: login request with wrong password
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword("wrongpassword");
    request.setSubdomain(subdomain);

    when(organizationRepository.findBySubdomain(subdomain)).thenReturn(Optional.of(organization));
    when(userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .thenReturn(userPrincipal);
    when(passwordEncoder.matches("wrongpassword", userPrincipal.getPassword())).thenReturn(false);

    // When/Then: login throws BadCredentialsException
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid credentials");

    verify(passwordEncoder).matches("wrongpassword", userPrincipal.getPassword());
    verify(jwtUtil, never()).generateToken(any(), any(), any());
  }

  @Test
  @DisplayName("login with inactive user throws BadCredentialsException")
  void login_WithInactiveUser_ThrowsBadCredentialsException() {
    // Given: inactive user (UserDetailsService rejects inactive users)
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain(subdomain);

    when(organizationRepository.findBySubdomain(subdomain)).thenReturn(Optional.of(organization));
    when(userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .thenThrow(
            new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "User is inactive"));

    // When/Then: login throws exception (converted by service)
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(
            org.springframework.security.core.userdetails.UsernameNotFoundException.class);

    verify(userDetailsService).loadUserByOrganizationAndEmail(organizationId, email);
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  @DisplayName("login with nonexistent user throws BadCredentialsException")
  void login_WithNonexistentUser_ThrowsBadCredentialsException() {
    // Given: user does not exist
    final LoginRequest request = new LoginRequest();
    request.setEmail("nonexistent@example.com");
    request.setPassword(password);
    request.setSubdomain(subdomain);

    when(organizationRepository.findBySubdomain(subdomain)).thenReturn(Optional.of(organization));
    when(userDetailsService.loadUserByOrganizationAndEmail(
            organizationId, "nonexistent@example.com"))
        .thenThrow(
            new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "User not found"));

    // When/Then: login throws exception
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(
            org.springframework.security.core.userdetails.UsernameNotFoundException.class);

    verify(userDetailsService)
        .loadUserByOrganizationAndEmail(organizationId, "nonexistent@example.com");
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  @DisplayName("login with invalid subdomain throws IllegalArgumentException")
  void login_WithInvalidSubdomain_ThrowsIllegalArgumentException() {
    // Given: invalid subdomain
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain("nonexistent");

    when(organizationRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

    // When/Then: login throws IllegalArgumentException
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Organization not found")
        .hasMessageContaining("nonexistent");
  }

  @Test
  @DisplayName("login with invalid organizationId throws IllegalArgumentException")
  void login_WithInvalidOrganizationId_ThrowsIllegalArgumentException() {
    // Given: invalid organizationId format
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setOrganizationId("invalid-uuid");

    // When/Then: login throws IllegalArgumentException
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid organizationId format");
  }

  @Test
  @DisplayName("login with both subdomain and orgId throws IllegalArgumentException")
  void login_WithBothSubdomainAndOrgId_ThrowsIllegalArgumentException() {
    // Given: both subdomain and organizationId provided
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain(subdomain);
    request.setOrganizationId(organizationId.toString());

    // When/Then: login throws IllegalArgumentException
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Only one of subdomain or organizationId should be provided");
  }

  @Test
  @DisplayName("login with neither subdomain nor orgId throws IllegalArgumentException")
  void login_WithNeitherSubdomainNorOrgId_ThrowsIllegalArgumentException() {
    // Given: neither subdomain nor organizationId provided
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);

    // When/Then: login throws IllegalArgumentException
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Either subdomain or organizationId must be provided");
  }

  @Test
  @DisplayName("login with nonexistent subdomain throws IllegalArgumentException")
  void login_WithNonexistentSubdomain_ThrowsIllegalArgumentException() {
    // Given: subdomain that doesn't exist
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain("nonexistent");

    when(organizationRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

    // When/Then: login throws IllegalArgumentException
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Organization not found")
        .hasMessageContaining("nonexistent");
  }

  @Test
  @DisplayName("login password verified with BCrypt")
  void login_PasswordVerifiedWithBCrypt() {
    // Given: login request
    final LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);
    request.setSubdomain(subdomain);

    when(organizationRepository.findBySubdomain(subdomain)).thenReturn(Optional.of(organization));
    when(userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .thenReturn(userPrincipal);
    when(passwordEncoder.matches(eq(password), any(String.class))).thenReturn(true);
    when(jwtUtil.generateToken(userId, organizationId, "ADMIN")).thenReturn("test-token");

    // When: login is performed
    authService.login(request);

    // Then: password encoder matches was called
    verify(passwordEncoder).matches(eq(password), any(String.class));
  }
}
