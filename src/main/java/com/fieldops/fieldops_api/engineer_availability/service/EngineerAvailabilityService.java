package com.fieldops.fieldops_api.engineer_availability.service;

import com.fieldops.fieldops_api.engineer_availability.domain.EngineerAvailability;
import com.fieldops.fieldops_api.engineer_availability.model.EngineerAvailabilityDTO;
import com.fieldops.fieldops_api.engineer_availability.repos.EngineerAvailabilityRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EngineerAvailabilityService {

  private final EngineerAvailabilityRepository engineerAvailabilityRepository;
  private final OrganizationRepository organizationRepository;

  public EngineerAvailabilityService(
      final EngineerAvailabilityRepository engineerAvailabilityRepository,
      final OrganizationRepository organizationRepository) {
    this.engineerAvailabilityRepository = engineerAvailabilityRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<EngineerAvailabilityDTO> findAll() {
    final List<EngineerAvailability> engineerAvailabilities =
        engineerAvailabilityRepository.findAll(Sort.by("id"));
    return engineerAvailabilities.stream()
        .map(engineerAvailability -> mapToDTO(engineerAvailability, new EngineerAvailabilityDTO()))
        .toList();
  }

  public EngineerAvailabilityDTO get(final UUID id) {
    return engineerAvailabilityRepository
        .findById(id)
        .map(engineerAvailability -> mapToDTO(engineerAvailability, new EngineerAvailabilityDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final EngineerAvailabilityDTO engineerAvailabilityDTO) {
    final EngineerAvailability engineerAvailability = new EngineerAvailability();
    mapToEntity(engineerAvailabilityDTO, engineerAvailability);
    return engineerAvailabilityRepository.save(engineerAvailability).getId();
  }

  public void update(final UUID id, final EngineerAvailabilityDTO engineerAvailabilityDTO) {
    final EngineerAvailability engineerAvailability =
        engineerAvailabilityRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(engineerAvailabilityDTO, engineerAvailability);
    engineerAvailabilityRepository.save(engineerAvailability);
  }

  public void delete(final UUID id) {
    final EngineerAvailability engineerAvailability =
        engineerAvailabilityRepository.findById(id).orElseThrow(NotFoundException::new);
    engineerAvailabilityRepository.delete(engineerAvailability);
  }

  private EngineerAvailabilityDTO mapToDTO(
      final EngineerAvailability engineerAvailability,
      final EngineerAvailabilityDTO engineerAvailabilityDTO) {
    engineerAvailabilityDTO.setId(engineerAvailability.getId());
    engineerAvailabilityDTO.setAvailabilityType(engineerAvailability.getAvailabilityType());
    engineerAvailabilityDTO.setDateCreated(engineerAvailability.getDateCreated());
    engineerAvailabilityDTO.setEndTime(engineerAvailability.getEndTime());
    engineerAvailabilityDTO.setLastUpdated(engineerAvailability.getLastUpdated());
    engineerAvailabilityDTO.setNotes(engineerAvailability.getNotes());
    engineerAvailabilityDTO.setStartTime(engineerAvailability.getStartTime());
    engineerAvailabilityDTO.setEngineerUserId(engineerAvailability.getEngineerUserId());
    engineerAvailabilityDTO.setOrganization(
        engineerAvailability.getOrganization() == null
            ? null
            : engineerAvailability.getOrganization().getId());
    return engineerAvailabilityDTO;
  }

  private EngineerAvailability mapToEntity(
      final EngineerAvailabilityDTO engineerAvailabilityDTO,
      final EngineerAvailability engineerAvailability) {
    engineerAvailability.setAvailabilityType(engineerAvailabilityDTO.getAvailabilityType());
    engineerAvailability.setEndTime(engineerAvailabilityDTO.getEndTime());
    engineerAvailability.setNotes(engineerAvailabilityDTO.getNotes());
    engineerAvailability.setStartTime(engineerAvailabilityDTO.getStartTime());
    engineerAvailability.setEngineerUserId(engineerAvailabilityDTO.getEngineerUserId());
    final Organization organization =
        engineerAvailabilityDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(engineerAvailabilityDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    engineerAvailability.setOrganization(organization);
    return engineerAvailability;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final EngineerAvailability organizationEngineerAvailability =
        engineerAvailabilityRepository.findFirstByOrganizationId(event.getId());
    if (organizationEngineerAvailability != null) {
      referencedException.setKey("organization.engineerAvailability.organization.referenced");
      referencedException.addParam(organizationEngineerAvailability.getId());
      throw referencedException;
    }
  }
}
