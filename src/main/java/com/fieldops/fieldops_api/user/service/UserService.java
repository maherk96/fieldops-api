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

@Service
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final ApplicationEventPublisher publisher;

  public UserService(final UserRepository userRepository, final ApplicationEventPublisher publisher) {
    this.userRepository = userRepository;
    this.publisher = publisher;
  }

  @Cacheable(cacheNames = "usersList")
  public List<UserDTO> findAll() {
    final List<User> users = userRepository.findAll(Sort.by("id"));
    return users.stream().map(UserService::toDTO).toList();
  }

  @Cacheable(cacheNames = "usersById", key = "#id")
  public UserDTO get(final UUID id) {
    return userRepository.findById(id).map(UserService::toDTO).orElseThrow(NotFoundException::new);
  }

  @Cacheable(cacheNames = "userExistsByEmail", key = "#email")
  public boolean existsByEmail(final String email) {
    return userRepository.findByEmail(email).isPresent();
  }

  @Transactional
  @Caching(
          evict = {
                  @CacheEvict(cacheNames = "usersList", allEntries = true),
                  @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                  @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
          })
  public UUID create(final UserDTO userDTO) {
    final User user = new User();
    applyEditableFields(userDTO, user);
    return userRepository.save(user).getId();
  }

  @Transactional
  @Caching(
          evict = {
                  @CacheEvict(cacheNames = "usersById", key = "#id"),
                  @CacheEvict(cacheNames = "usersList", allEntries = true),
                  @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                  @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
          })
  public void update(final UUID id, final UserDTO userDTO) {
    final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
    applyEditableFields(userDTO, user);
    userRepository.save(user);
  }

  @Transactional
  @Caching(
          evict = {
                  @CacheEvict(cacheNames = "usersById", key = "#id"),
                  @CacheEvict(cacheNames = "usersList", allEntries = true),
                  @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                  @CacheEvict(cacheNames = "userExistsByEmail", allEntries = true)
          })
  public void delete(final UUID id) {
    final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
    publisher.publishEvent(new BeforeDeleteUser(id));
    userRepository.delete(user);
  }

  private static UserDTO toDTO(final User user) {
    final UserDTO dto = new UserDTO();
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

  private static void applyEditableFields(final UserDTO userDTO, final User user) {
    if (userDTO.getEmail() != null) {
      user.setEmail(userDTO.getEmail().trim().toLowerCase());
    }
    user.setFullName(userDTO.getFullName());
    user.setRole(userDTO.getRole());
    user.setActive(userDTO.getActive());
  }
}