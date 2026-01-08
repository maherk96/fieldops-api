package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for admin-style user management operations such as creating users and
 * updating user details.
 *
 * <p>This class focuses on write operations and ensures relevant user-related caches are evicted
 * after mutations.
 */
@Service
public class UserManagementService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserService userService;

  /**
   * Creates a {@link UserManagementService}.
   *
   * @param userRepository repository used to persist and query {@link User} entities
   * @param passwordEncoder encoder used to hash raw passwords before persistence
   * @param userService service used for lookups and DTO mapping after persistence
   */
  public UserManagementService(
      final UserRepository userRepository,
      final PasswordEncoder passwordEncoder,
      final UserService userService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userService = userService;
  }

  /**
   * Admin use-case: create a user including a password.
   *
   * <p>Behaviour:
   *
   * <ul>
   *   <li>Normalises email (trim + lowercase)
   *   <li>Encodes the provided raw password before storing
   *   <li>Defaults {@code fullName} to email if not supplied
   *   <li>Defaults {@code active} to {@code true} if not supplied
   *   <li>Evicts relevant caches so subsequent reads reflect the new user
   * </ul>
   *
   * <p>Uniqueness/race handling:
   *
   * <ul>
   *   <li>Performs a fast existence check to provide a friendly error
   *   <li>Still guards against concurrent creates by catching {@link
   *       DataIntegrityViolationException}
   * </ul>
   *
   * @param request user creation payload
   * @return the created user as a {@link UserDTO}
   * @throws IllegalArgumentException if the email already exists
   */
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "usersList", allEntries = true),
        @CacheEvict(cacheNames = "userExistsByEmail", key = "#request.email"),
        @CacheEvict(cacheNames = "usersByEmail", key = "#request.email")
      })
  public UserDTO createUserWithPassword(final CreateUserRequest request) {
    final var email = request.getEmail().trim().toLowerCase();

    if (userService.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already exists");
    }

    final var now = OffsetDateTime.now();

    final var user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.getPassword()));

    final var fullName = request.getFullName() != null ? request.getFullName() : email;
    user.setFullName(fullName);

    user.setRole(request.getRole());

    final var active = request.getActive() != null ? request.getActive() : Boolean.TRUE;
    user.setActive(active);

    user.setCreatedAt(now);
    user.setUpdatedAt(now);

    try {
      final var saved = userRepository.save(user);

      return userService.get(saved.getId());

    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("Email already exists");
    }
  }

  /**
   * Updates a user's mutable fields.
   *
   * <p>This method performs a partial update: only non-null fields in {@code userDTO} are applied.
   *
   * <p>Authorization rules are not enforced here (yet). Typically:
   *
   * <ul>
   *   <li>SELF: allow only {@code fullName} (and possibly email later)
   *   <li>ADMIN: allow role/active/etc.
   * </ul>
   *
   * <p>Cache eviction is broad to ensure any list/search caches are invalidated.
   *
   * @param id user ID to update
   * @param userDTO fields to update (null fields are ignored)
   * @throws NotFoundException if no user exists for the supplied ID
   */
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "usersList", allEntries = true),
        @CacheEvict(cacheNames = "usersById", key = "#id"),
        @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
        @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
      })
  public void updateUser(final UUID id, final UserDTO userDTO) {
    final var user = userRepository.findById(id).orElseThrow(NotFoundException::new);

    if (userDTO.getFullName() != null) {
      user.setFullName(userDTO.getFullName());
    }
    if (userDTO.getActive() != null) {
      user.setActive(userDTO.getActive());
    }
    if (userDTO.getRole() != null) {
      user.setRole(userDTO.getRole());
    }

    user.setUpdatedAt(OffsetDateTime.now());
    userRepository.save(user);
  }
}
