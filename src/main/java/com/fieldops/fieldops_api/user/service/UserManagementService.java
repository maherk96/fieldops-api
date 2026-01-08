package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.CreateUserRequest;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import java.time.OffsetDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserService userService;

  public UserManagementService(
          final UserRepository userRepository,
          final PasswordEncoder passwordEncoder,
          final UserService userService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userService = userService;
  }

  /**
   * Admin use-case: create a user with a password.
   *
   * Notes:
   * - Do NOT set version/changeVersion manually; let JPA/DB defaults handle it.
   * - Still handle unique-email race via DataIntegrityViolationException.
   */
  @Transactional
  @Caching(
          evict = {
                  @CacheEvict(cacheNames = "usersList", allEntries = true),
                  @CacheEvict(cacheNames = "userExistsByEmail", key = "#request.email"),
                  @CacheEvict(cacheNames = "usersByEmail", key = "#request.email")
          })
  public UserDTO createUserWithPassword(final CreateUserRequest request) {
    final String email = request.getEmail().trim().toLowerCase();

    // Fast path (nice error message) - still not sufficient alone under concurrency.
    if (userService.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already exists");
    }

    final OffsetDateTime now = OffsetDateTime.now();

    final User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFullName(request.getFullName());
    user.setRole(request.getRole());
    user.setActive(request.getActive());
    user.setCreatedAt(now);
    user.setUpdatedAt(now);

    try {
      final User saved = userRepository.save(user);

      // Reuse existing mapping logic (keeps DTO consistent in one place)
      // If you add caching to UserService.get(id), this also populates it.
      return userService.get(saved.getId());

    } catch (DataIntegrityViolationException e) {
      // Handles race: two concurrent creates with same email.
      throw new IllegalArgumentException("Email already exists");
    }
  }

  /**
   * Update use-case (called from controller).
   * Implement field-level rules here:
   * - If caller is SELF: allow only fullName (and maybe email later)
   * - If caller is ADMIN: allow role/active/etc.
   *
   * (Add Authentication context or pass actor info if you want strict enforcement.)
   */
  @Transactional
  @Caching(
          evict = {
                  @CacheEvict(cacheNames = "usersList", allEntries = true),
                  @CacheEvict(cacheNames = "usersById", key = "#id"),
                  @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                  @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
          })
  public void updateUser(final java.util.UUID id, final UserDTO userDTO) {
    final User user = userRepository.findById(id).orElseThrow(com.fieldops.fieldops_api.util.NotFoundException::new);

    // MVP: keep it simple. Later enforce role-based field rules here.
    user.setFullName(userDTO.getFullName());
    user.setActive(userDTO.getActive());
    user.setRole(userDTO.getRole());
    user.setUpdatedAt(OffsetDateTime.now());

    userRepository.save(user);
  }
}