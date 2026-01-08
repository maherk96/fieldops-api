package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read/write service for {@link User} entities.
 *
 * <p>This service provides:
 *
 * <ul>
 *   <li>Cached read operations (list, by id, existence checks)
 *   <li>Transactional write operations (create, update, delete) with cache eviction
 *   <li>DTO mapping to keep controllers free of entity concerns
 * </ul>
 *
 * <p>Note: This service's default transaction mode is {@code readOnly=true}. Mutating methods
 * explicitly override this with {@link Transactional}.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final ApplicationEventPublisher publisher;

  /**
   * Creates a {@link UserService}.
   *
   * @param userRepository repository used to query and persist {@link User} entities
   * @param publisher application event publisher for lifecycle events
   */
  public UserService(
      final UserRepository userRepository, final ApplicationEventPublisher publisher) {
    this.userRepository = userRepository;
    this.publisher = publisher;
  }

  /**
   * Returns all users sorted by {@code id}.
   *
   * <p>This method is cached using {@code usersList}.
   *
   * @return list of all users as DTOs
   */
  @Cacheable(cacheNames = "usersList")
  public List<UserDTO> findAll() {
    final var users = userRepository.findAll(Sort.by("id"));
    return users.stream().map(UserService::toDTO).toList();
  }

  /**
   * Returns a single user by ID.
   *
   * <p>This method is cached using {@code usersById}.
   *
   * @param id user ID
   * @return user DTO
   * @throws NotFoundException if no user exists for the given ID
   */
  @Cacheable(cacheNames = "usersById", key = "#id")
  public UserDTO get(final UUID id) {
    return userRepository.findById(id).map(UserService::toDTO).orElseThrow(NotFoundException::new);
  }

  /**
   * Checks if a user exists for the given email.
   *
   * <p>This method is cached using {@code userExistsByEmail}. Callers should ensure the email is
   * normalised consistently (trim + lowercase) to maximise cache hits.
   *
   * @param email email to check
   * @return true if a user exists with that email
   */
  @Cacheable(cacheNames = "userExistsByEmail", key = "#email")
  public boolean existsByEmail(final String email) {
    return userRepository.findByEmail(email).isPresent();
  }

  /**
   * Creates a new user from the provided DTO.
   *
   * <p>Evicts caches that might contain stale user lists or email lookups.
   *
   * @param userDTO DTO containing editable fields to apply
   * @return the new user ID
   */
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "usersList", allEntries = true),
        @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
        @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
      })
  public UUID create(final UserDTO userDTO) {
    final var user = new User();
    applyEditableFields(userDTO, user);
    return userRepository.save(user).getId();
  }

  /**
   * Updates an existing user using the provided DTO.
   *
   * <p>Evicts caches that might contain stale user data.
   *
   * @param id ID of the user to update
   * @param userDTO DTO containing editable fields to apply
   * @throws NotFoundException if no user exists for the given ID
   */
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "usersById", key = "#id"),
        @CacheEvict(cacheNames = "usersList", allEntries = true),
        @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
        @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
      })
  public void update(final UUID id, final UserDTO userDTO) {
    final var user = userRepository.findById(id).orElseThrow(NotFoundException::new);
    applyEditableFields(userDTO, user);
    userRepository.save(user);
  }

  /**
   * Deletes a user by ID.
   *
   * <p>Publishes a {@link BeforeDeleteUser} event before deletion to allow other parts of the
   * system to clean up related data.
   *
   * <p>Evicts caches that might contain stale user data.
   *
   * @param id ID of the user to delete
   * @throws NotFoundException if no user exists for the given ID
   */
  @Transactional
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "usersById", key = "#id"),
        @CacheEvict(cacheNames = "usersList", allEntries = true),
        @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
        @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
      })
  public void delete(final UUID id) {
    final var user = userRepository.findById(id).orElseThrow(NotFoundException::new);

    publisher.publishEvent(new BeforeDeleteUser(id));
    userRepository.delete(user);
  }

  /**
   * Maps a {@link User} entity to a {@link UserDTO}.
   *
   * @param user entity to map
   * @return mapped DTO
   */
  private static UserDTO toDTO(final User user) {
    final var dto = new UserDTO();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setFullName(user.getFullName());
    dto.setRole(user.getRole());
    dto.setActive(user.getActive());

    dto.setVersion(user.getVersion());
    dto.setChangeVersion(user.getChangeVersion());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setUpdatedAt(user.getUpdatedAt());

    return dto;
  }

  /**
   * Applies editable fields from a DTO onto an entity.
   *
   * <p>Email is normalised (trim + lowercase) when provided. Other fields are applied as-is.
   *
   * @param userDTO source DTO
   * @param user target entity
   */
  private static void applyEditableFields(final UserDTO userDTO, final User user) {
    if (userDTO.getEmail() != null) {
      user.setEmail(userDTO.getEmail().trim().toLowerCase());
    }
    user.setFullName(userDTO.getFullName());
    user.setRole(userDTO.getRole());
    user.setActive(userDTO.getActive());
  }
}
