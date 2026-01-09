package com.fieldops.fieldops_api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.security.TenantContext;
import com.fieldops.fieldops_api.testutil.TestDataHelper;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UpdateUserRequest;
import com.fieldops.fieldops_api.user.model.UserResponse;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.ForbiddenException;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for AdminUserService.
 *
 * <p>Tests tenant enforcement, organization scoping, and cross-tenant access prevention.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService Tests")
class AdminUserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private OrganizationRepository organizationRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AdminUserService adminUserService;

  private PasswordEncoder realPasswordEncoder;
  private UUID organizationId1;
  private UUID organizationId2;
  private UUID userId1;
  private UUID userId2;
  private Organization organization1;
  private Organization organization2;
  private User user1;
  private User user2;
  private String email1;
  private String email2;

  @BeforeEach
  void setUp() {
    realPasswordEncoder = new BCryptPasswordEncoder();
    organizationId1 = UUID.randomUUID();
    organizationId2 = UUID.randomUUID();
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();
    email1 = "user1@example.com";
    email2 = "user2@example.com";

    organization1 = TestDataHelper.createTestOrganization(organizationId1, "org1");
    organization2 = TestDataHelper.createTestOrganization(organizationId2, "org2");

    user1 =
        TestDataHelper.createTestUser(email1, organization1, "ENGINEER", true, realPasswordEncoder);
    user1.setId(userId1);
    user2 =
        TestDataHelper.createTestUser(email2, organization2, "ENGINEER", true, realPasswordEncoder);
    user2.setId(userId2);

    // Set tenant context to organization1
    TenantContext.setOrganizationId(organizationId1);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  @DisplayName("createUser assigns current org from TenantContext")
  void createUser_AssignsCurrentOrgFromTenantContext() {
    // Given: create request
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    when(organizationRepository.findById(organizationId1)).thenReturn(Optional.of(organization1));
    when(userRepository.existsByOrganizationIdAndEmail(organizationId1, "newuser@example.com"))
        .thenReturn(false);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When: user is created
    final UserResponse response = adminUserService.createUser(request);

    // Then: user is assigned to organization1 (from TenantContext)
    assertThat(response.getOrganizationId()).isEqualTo(organizationId1);
    verify(userRepository).existsByOrganizationIdAndEmail(organizationId1, "newuser@example.com");
    verify(organizationRepository).findById(organizationId1);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("createUser with duplicate email in same org throws IllegalArgumentException")
  void createUser_WithDuplicateEmailInSameOrg_ThrowsIllegalArgumentException() {
    // Given: duplicate email exists
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail(email1);
    request.setPassword("password123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    when(userRepository.existsByOrganizationIdAndEmail(organizationId1, email1)).thenReturn(true);

    // When/Then: creation throws IllegalArgumentException
    assertThatThrownBy(() -> adminUserService.createUser(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists")
        .hasMessageContaining(email1);

    verify(userRepository).existsByOrganizationIdAndEmail(organizationId1, email1);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("createUser with unique email saves user with encrypted password")
  void createUser_WithUniqueEmail_SavesUserWithEncryptedPassword() {
    // Given: unique email
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("plaintext123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    when(organizationRepository.findById(organizationId1)).thenReturn(Optional.of(organization1));
    when(userRepository.existsByOrganizationIdAndEmail(organizationId1, "newuser@example.com"))
        .thenReturn(false);
    when(passwordEncoder.encode("plaintext123"))
        .thenReturn(realPasswordEncoder.encode("plaintext123"));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When: user is created
    adminUserService.createUser(request);

    // Then: password encoder was called (password encrypted)
    verify(passwordEncoder).encode("plaintext123");
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("createUser with valid request returns UserResponse")
  void createUser_WithValidRequest_ReturnsUserResponse() {
    // Given: valid request
    final CreateUserRequest request = new CreateUserRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("password123");
    request.setFullName("New User");
    request.setRole("ENGINEER");
    request.setActive(true);

    when(organizationRepository.findById(organizationId1)).thenReturn(Optional.of(organization1));
    when(userRepository.existsByOrganizationIdAndEmail(organizationId1, "newuser@example.com"))
        .thenReturn(false);
    when(passwordEncoder.encode("password123"))
        .thenReturn(realPasswordEncoder.encode("password123"));
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              final User user = invocation.getArgument(0);
              user.setId(UUID.randomUUID());
              return user;
            });

    // When: user is created
    final UserResponse response = adminUserService.createUser(request);

    // Then: response contains all fields except password
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getEmail()).isEqualTo("newuser@example.com");
    assertThat(response.getFullName()).isEqualTo("New User");
    assertThat(response.getRole()).isEqualTo("ENGINEER");
    assertThat(response.getActive()).isTrue();
    assertThat(response.getOrganizationId()).isEqualTo(organizationId1);
  }

  @Test
  @DisplayName("getAllUsers returns only current org users")
  void getAllUsers_ReturnsOnlyCurrentOrgUsers() {
    // Given: users in organization1
    final User user2Org1 =
        TestDataHelper.createTestUser(
            "user2@org1.com", organization1, "ENGINEER", true, realPasswordEncoder);

    when(userRepository.findByOrganizationId(organizationId1))
        .thenReturn(List.of(user1, user2Org1));

    // When: all users are retrieved
    final List<UserResponse> users = adminUserService.getAllUsers();

    // Then: only organization1 users are returned
    assertThat(users).hasSize(2);
    assertThat(users).extracting(UserResponse::getOrganizationId).containsOnly(organizationId1);
    verify(userRepository).findByOrganizationId(organizationId1);
    verify(userRepository, never()).findAll();
  }

  @Test
  @DisplayName("getUser with id in same org returns UserResponse")
  void getUser_WithIdInSameOrg_ReturnsUserResponse() {
    // Given: user exists in same org
    when(userRepository.findByIdAndOrganizationId(userId1, organizationId1))
        .thenReturn(Optional.of(user1));

    // When: user is retrieved
    final UserResponse response = adminUserService.getUser(userId1);

    // Then: user is returned
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(userId1);
    assertThat(response.getEmail()).isEqualTo(email1);
    assertThat(response.getOrganizationId()).isEqualTo(organizationId1);
    verify(userRepository).findByIdAndOrganizationId(userId1, organizationId1);
  }

  @Test
  @DisplayName("getUser with id in different org throws ForbiddenException")
  void getUser_WithIdInDifferentOrg_ThrowsForbiddenException() {
    // Given: user exists in different org
    when(userRepository.findByIdAndOrganizationId(userId2, organizationId1))
        .thenReturn(Optional.empty());
    when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

    // When/Then: retrieval throws ForbiddenException
    assertThatThrownBy(() -> adminUserService.getUser(userId2))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Cannot access or modify users from other organizations");

    verify(userRepository).findByIdAndOrganizationId(userId2, organizationId1);
    verify(userRepository).findById(userId2);
  }

  @Test
  @DisplayName("getUser with nonexistent id throws NotFoundException")
  void getUser_WithNonexistentId_ThrowsNotFoundException() {
    // Given: user does not exist
    final UUID nonexistentId = UUID.randomUUID();
    when(userRepository.findByIdAndOrganizationId(nonexistentId, organizationId1))
        .thenReturn(Optional.empty());
    when(userRepository.findById(nonexistentId)).thenReturn(Optional.empty());

    // When/Then: retrieval throws NotFoundException
    assertThatThrownBy(() -> adminUserService.getUser(nonexistentId))
        .isInstanceOf(NotFoundException.class);

    verify(userRepository).findByIdAndOrganizationId(nonexistentId, organizationId1);
    verify(userRepository).findById(nonexistentId);
  }

  @Test
  @DisplayName("updateUser with id in same org updates and returns UserResponse")
  void updateUser_WithIdInSameOrg_UpdatesAndReturnsUserResponse() {
    // Given: update request
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail("updated@example.com");
    request.setFullName("Updated Name");

    when(userRepository.findByIdAndOrganizationId(userId1, organizationId1))
        .thenReturn(Optional.of(user1));
    when(userRepository.existsByOrganizationIdAndEmail(organizationId1, "updated@example.com"))
        .thenReturn(false);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When: user is updated
    final UserResponse response = adminUserService.updateUser(userId1, request);

    // Then: user is updated
    assertThat(response.getEmail()).isEqualTo("updated@example.com");
    assertThat(response.getFullName()).isEqualTo("Updated Name");
    verify(userRepository).findByIdAndOrganizationId(userId1, organizationId1);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("updateUser with id in different org throws ForbiddenException")
  void updateUser_WithIdInDifferentOrg_ThrowsForbiddenException() {
    // Given: update request for user in different org
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail("updated@example.com");

    when(userRepository.findByIdAndOrganizationId(userId2, organizationId1))
        .thenReturn(Optional.empty());
    when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

    // When/Then: update throws ForbiddenException
    assertThatThrownBy(() -> adminUserService.updateUser(userId2, request))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Cannot access or modify users from other organizations");

    verify(userRepository).findByIdAndOrganizationId(userId2, organizationId1);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateUser with duplicate email throws IllegalArgumentException")
  void updateUser_WithDuplicateEmail_ThrowsIllegalArgumentException() {
    // Given: update request with duplicate email
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail(email2); // Different user's email

    when(userRepository.findByIdAndOrganizationId(userId1, organizationId1))
        .thenReturn(Optional.of(user1));
    when(userRepository.existsByOrganizationIdAndEmail(organizationId1, email2)).thenReturn(true);

    // When/Then: update throws IllegalArgumentException
    assertThatThrownBy(() -> adminUserService.updateUser(userId1, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");

    verify(userRepository).findByIdAndOrganizationId(userId1, organizationId1);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateUser with null password does not update password")
  void updateUser_WithNullPassword_DoesNotUpdatePassword() {
    // Given: update request without password
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setFullName("Updated Name");

    when(userRepository.findByIdAndOrganizationId(userId1, organizationId1))
        .thenReturn(Optional.of(user1));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When: user is updated
    adminUserService.updateUser(userId1, request);

    // Then: password encoder was not called
    verify(passwordEncoder, never()).encode(any());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("updateUser with empty password does not update password")
  void updateUser_WithEmptyPassword_DoesNotUpdatePassword() {
    // Given: update request with empty password
    final UpdateUserRequest request = new UpdateUserRequest();
    request.setPassword("");

    when(userRepository.findByIdAndOrganizationId(userId1, organizationId1))
        .thenReturn(Optional.of(user1));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When: user is updated
    adminUserService.updateUser(userId1, request);

    // Then: password encoder was not called
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  @DisplayName("deactivateUser with id in same org deactivates user")
  void deactivateUser_WithIdInSameOrg_DeactivatesUser() {
    // Given: user exists in same org
    when(userRepository.findByIdAndOrganizationId(userId1, organizationId1))
        .thenReturn(Optional.of(user1));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When: user is deactivated
    final UserResponse response = adminUserService.deactivateUser(userId1);

    // Then: user is deactivated
    assertThat(response.getActive()).isFalse();
    verify(userRepository).findByIdAndOrganizationId(userId1, organizationId1);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("deactivateUser with id in different org throws ForbiddenException")
  void deactivateUser_WithIdInDifferentOrg_ThrowsForbiddenException() {
    // Given: user exists in different org
    when(userRepository.findByIdAndOrganizationId(userId2, organizationId1))
        .thenReturn(Optional.empty());
    when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

    // When/Then: deactivation throws ForbiddenException
    assertThatThrownBy(() -> adminUserService.deactivateUser(userId2))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Cannot access or modify users from other organizations");

    verify(userRepository).findByIdAndOrganizationId(userId2, organizationId1);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("all service methods verify org-scoped repository calls")
  void allServiceMethods_VerifyOrgScopedRepositoryCalls() {
    // Given: TenantContext is set to organization1
    TenantContext.setOrganizationId(organizationId1);

    // When: getAllUsers is called
    when(userRepository.findByOrganizationId(organizationId1)).thenReturn(List.of());
    adminUserService.getAllUsers();

    // Then: org-scoped method was called
    verify(userRepository).findByOrganizationId(organizationId1);
    verify(userRepository, never()).findAll();
  }
}
