package com.fieldops.fieldops_api.user.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.model.UserDTO;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final ApplicationEventPublisher publisher;

  public UserService(
      final UserRepository userRepository,
      final OrganizationRepository organizationRepository,
      final ApplicationEventPublisher publisher) {
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    this.publisher = publisher;
  }

  public List<UserDTO> findAll() {
    final List<User> users = userRepository.findAll(Sort.by("id"));
    return users.stream().map(user -> mapToDTO(user, new UserDTO())).toList();
  }

  public UserDTO get(final UUID id) {
    return userRepository
        .findById(id)
        .map(user -> mapToDTO(user, new UserDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final UserDTO userDTO) {
    final User user = new User();
    mapToEntity(userDTO, user);
    return userRepository.save(user).getId();
  }

  public void update(final UUID id, final UserDTO userDTO) {
    final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(userDTO, user);
    userRepository.save(user);
  }

  public void delete(final UUID id) {
    final User user = userRepository.findById(id).orElseThrow(NotFoundException::new);
    publisher.publishEvent(new BeforeDeleteUser(id));
    userRepository.delete(user);
  }

  private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
    userDTO.setId(user.getId());
    userDTO.setActive(user.getActive());
    userDTO.setChangeVersion(user.getChangeVersion());
    userDTO.setEmail(user.getEmail());
    userDTO.setFullName(user.getFullName());
    userDTO.setPassword(user.getPassword());
    userDTO.setRole(user.getRole());
    userDTO.setUpdatedAt(user.getUpdatedAt());
    userDTO.setVersion(user.getVersion());
    userDTO.setOrganization(user.getOrganization() == null ? null : user.getOrganization().getId());
    return userDTO;
  }

  private User mapToEntity(final UserDTO userDTO, final User user) {
    user.setActive(userDTO.getActive());
    user.setChangeVersion(userDTO.getChangeVersion());
    user.setEmail(userDTO.getEmail());
    user.setFullName(userDTO.getFullName());
    user.setPassword(userDTO.getPassword());
    user.setRole(userDTO.getRole());
    user.setUpdatedAt(userDTO.getUpdatedAt());
    user.setVersion(userDTO.getVersion());
    final Organization organization =
        userDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(userDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    user.setOrganization(organization);
    return user;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final User organizationUser = userRepository.findFirstByOrganizationId(event.getId());
    if (organizationUser != null) {
      referencedException.setKey("organization.user.organization.referenced");
      referencedException.addParam(organizationUser.getId());
      throw referencedException;
    }
  }
}
