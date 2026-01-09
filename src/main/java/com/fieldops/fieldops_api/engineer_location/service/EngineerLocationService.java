package com.fieldops.fieldops_api.engineer_location.service;

import com.fieldops.fieldops_api.engineer_location.domain.EngineerLocation;
import com.fieldops.fieldops_api.engineer_location.model.EngineerLocationDTO;
import com.fieldops.fieldops_api.engineer_location.repos.EngineerLocationRepository;
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
public class EngineerLocationService {

  private final EngineerLocationRepository engineerLocationRepository;
  private final OrganizationRepository organizationRepository;

  public EngineerLocationService(
      final EngineerLocationRepository engineerLocationRepository,
      final OrganizationRepository organizationRepository) {
    this.engineerLocationRepository = engineerLocationRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<EngineerLocationDTO> findAll() {
    final List<EngineerLocation> engineerLocations =
        engineerLocationRepository.findAll(Sort.by("id"));
    return engineerLocations.stream()
        .map(engineerLocation -> mapToDTO(engineerLocation, new EngineerLocationDTO()))
        .toList();
  }

  public EngineerLocationDTO get(final UUID id) {
    return engineerLocationRepository
        .findById(id)
        .map(engineerLocation -> mapToDTO(engineerLocation, new EngineerLocationDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final EngineerLocationDTO engineerLocationDTO) {
    final EngineerLocation engineerLocation = new EngineerLocation();
    mapToEntity(engineerLocationDTO, engineerLocation);
    return engineerLocationRepository.save(engineerLocation).getId();
  }

  public void update(final UUID id, final EngineerLocationDTO engineerLocationDTO) {
    final EngineerLocation engineerLocation =
        engineerLocationRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(engineerLocationDTO, engineerLocation);
    engineerLocationRepository.save(engineerLocation);
  }

  public void delete(final UUID id) {
    final EngineerLocation engineerLocation =
        engineerLocationRepository.findById(id).orElseThrow(NotFoundException::new);
    engineerLocationRepository.delete(engineerLocation);
  }

  private EngineerLocationDTO mapToDTO(
      final EngineerLocation engineerLocation, final EngineerLocationDTO engineerLocationDTO) {
    engineerLocationDTO.setId(engineerLocation.getId());
    engineerLocationDTO.setAccuracyMeters(engineerLocation.getAccuracyMeters());
    engineerLocationDTO.setDateCreated(engineerLocation.getDateCreated());
    engineerLocationDTO.setLastUpdated(engineerLocation.getLastUpdated());
    engineerLocationDTO.setLat(engineerLocation.getLat());
    engineerLocationDTO.setLng(engineerLocation.getLng());
    engineerLocationDTO.setRecordedAt(engineerLocation.getRecordedAt());
    engineerLocationDTO.setEngineerUserId(engineerLocation.getEngineerUserId());
    engineerLocationDTO.setOrganization(
        engineerLocation.getOrganization() == null
            ? null
            : engineerLocation.getOrganization().getId());
    return engineerLocationDTO;
  }

  private EngineerLocation mapToEntity(
      final EngineerLocationDTO engineerLocationDTO, final EngineerLocation engineerLocation) {
    engineerLocation.setAccuracyMeters(engineerLocationDTO.getAccuracyMeters());
    engineerLocation.setLat(engineerLocationDTO.getLat());
    engineerLocation.setLng(engineerLocationDTO.getLng());
    engineerLocation.setRecordedAt(engineerLocationDTO.getRecordedAt());
    engineerLocation.setEngineerUserId(engineerLocationDTO.getEngineerUserId());
    final Organization organization =
        engineerLocationDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(engineerLocationDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    engineerLocation.setOrganization(organization);
    return engineerLocation;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final EngineerLocation organizationEngineerLocation =
        engineerLocationRepository.findFirstByOrganizationId(event.getId());
    if (organizationEngineerLocation != null) {
      referencedException.setKey("organization.engineerLocation.organization.referenced");
      referencedException.addParam(organizationEngineerLocation.getId());
      throw referencedException;
    }
  }
}
