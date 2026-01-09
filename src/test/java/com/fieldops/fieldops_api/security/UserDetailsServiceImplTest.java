package com.fieldops.fieldops_api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.testutil.TestDataHelper;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for UserDetailsServiceImpl.
 *
 * <p>Tests user loading by organization and email, including inactive user rejection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  private PasswordEncoder passwordEncoder;
  private UUID organizationId;
  private String email;
  private User activeUser;
  private User inactiveUser;
  private Organization organization;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    organizationId = UUID.randomUUID();
    email = "test@example.com";

    organization = TestDataHelper.createTestOrganization(organizationId, "testorg");
    activeUser = TestDataHelper.createTestUser(email, organization, "ADMIN", true, passwordEncoder);
    inactiveUser =
        TestDataHelper.createTestUser(email, organization, "ADMIN", false, passwordEncoder);
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with valid user returns UserPrincipal")
  void loadUserByOrganizationAndEmail_WithValidUser_ReturnsUserPrincipal() {
    // Given: user exists in repository
    when(userRepository.findByOrganizationIdAndEmail(eq(organizationId), eq(email)))
        .thenReturn(Optional.of(activeUser));

    // When: user is loaded
    final UserDetails userDetails =
        userDetailsService.loadUserByOrganizationAndEmail(organizationId, email);

    // Then: UserPrincipal is returned with correct fields
    assertThat(userDetails).isInstanceOf(UserPrincipal.class);
    final UserPrincipal userPrincipal = (UserPrincipal) userDetails;
    assertThat(userPrincipal.getId()).isEqualTo(activeUser.getId());
    assertThat(userPrincipal.getUsername()).isEqualTo(email);
    assertThat(userPrincipal.getOrganizationId()).isEqualTo(organizationId);
    assertThat(userPrincipal.getRole()).isEqualTo("ADMIN");
    assertThat(userPrincipal.isEnabled()).isTrue();
    assertThat(userPrincipal.getAuthorities()).hasSize(1);
    assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_ADMIN");
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with correct org and email returns UserDetails")
  void loadUserByOrganizationAndEmail_WithCorrectOrgAndEmail_ReturnsUserDetails() {
    // Given: user exists
    when(userRepository.findByOrganizationIdAndEmail(eq(organizationId), eq(email)))
        .thenReturn(Optional.of(activeUser));

    // When: user is loaded
    final UserDetails userDetails =
        userDetailsService.loadUserByOrganizationAndEmail(organizationId, email);

    // Then: UserDetails contains correct information
    assertThat(userDetails.getUsername()).isEqualTo(email);
    assertThat(userDetails.getPassword()).isEqualTo(activeUser.getPassword());
    assertThat(userDetails.isEnabled()).isTrue();
    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with wrong org throws UsernameNotFoundException")
  void loadUserByOrganizationAndEmail_WithWrongOrg_ThrowsUsernameNotFoundException() {
    // Given: user not found with wrong org
    final UUID wrongOrgId = UUID.randomUUID();
    when(userRepository.findByOrganizationIdAndEmail(eq(wrongOrgId), eq(email)))
        .thenReturn(Optional.empty());

    // When/Then: loading throws UsernameNotFoundException
    assertThatThrownBy(() -> userDetailsService.loadUserByOrganizationAndEmail(wrongOrgId, email))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found")
        .hasMessageContaining(email)
        .hasMessageContaining(wrongOrgId.toString());
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with wrong email throws UsernameNotFoundException")
  void loadUserByOrganizationAndEmail_WithWrongEmail_ThrowsUsernameNotFoundException() {
    // Given: user not found with wrong email
    final String wrongEmail = "wrong@example.com";
    when(userRepository.findByOrganizationIdAndEmail(eq(organizationId), eq(wrongEmail)))
        .thenReturn(Optional.empty());

    // When/Then: loading throws UsernameNotFoundException
    assertThatThrownBy(
            () -> userDetailsService.loadUserByOrganizationAndEmail(organizationId, wrongEmail))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found")
        .hasMessageContaining(wrongEmail);
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with inactive user throws UsernameNotFoundException")
  void loadUserByOrganizationAndEmail_WithInactiveUser_ThrowsUsernameNotFoundException() {
    // Given: inactive user exists
    when(userRepository.findByOrganizationIdAndEmail(eq(organizationId), eq(email)))
        .thenReturn(Optional.of(inactiveUser));

    // When/Then: loading throws UsernameNotFoundException
    assertThatThrownBy(
            () -> userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("inactive")
        .hasMessageContaining(email);
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with active=false rejects user")
  void loadUserByOrganizationAndEmail_WithActiveFalse_RejectsUser() {
    // Given: inactive user
    when(userRepository.findByOrganizationIdAndEmail(eq(organizationId), eq(email)))
        .thenReturn(Optional.of(inactiveUser));

    // When/Then: loading throws exception (user rejected)
    assertThatThrownBy(
            () -> userDetailsService.loadUserByOrganizationAndEmail(organizationId, email))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("inactive");
  }

  @Test
  @DisplayName("loadUserByUsername without org context throws UnsupportedOperationException")
  void loadUserByUsername_WithoutOrgContext_ThrowsUnsupportedOperationException() {
    // When/Then: loading throws UnsupportedOperationException
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("organization context");
  }

  @Test
  @DisplayName("loadUserByOrganizationAndEmail with ENGINEER role returns correct authorities")
  void loadUserByOrganizationAndEmail_WithEngineerRole_ReturnsCorrectAuthorities() {
    // Given: engineer user
    final User engineerUser =
        TestDataHelper.createTestUser(email, organization, "ENGINEER", true, passwordEncoder);
    when(userRepository.findByOrganizationIdAndEmail(eq(organizationId), eq(email)))
        .thenReturn(Optional.of(engineerUser));

    // When: user is loaded
    final UserDetails userDetails =
        userDetailsService.loadUserByOrganizationAndEmail(organizationId, email);

    // Then: authorities contain ROLE_ENGINEER
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
        .isEqualTo("ROLE_ENGINEER");
  }
}
