package com.fieldops.fieldops_api.engineer_last_location.service;

import com.fieldops.fieldops_api.engineer_last_location.domain.EngineerLastLocation;
import com.fieldops.fieldops_api.engineer_last_location.model.EngineerLastLocationDTO;
import com.fieldops.fieldops_api.engineer_last_location.repos.EngineerLastLocationRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EngineerLastLocationService {

  private final EngineerLastLocationRepository engineerLastLocationRepository;
  private final OrganizationRepository organizationRepository;

  public EngineerLastLocationService(
      final EngineerLastLocationRepository engineerLastLocationRepository,
      final OrganizationRepository organizationRepository) {
    this.engineerLastLocationRepository = engineerLastLocationRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<EngineerLastLocationDTO> findAll() {
    final List<EngineerLastLocation> engineerLastLocations =
        engineerLastLocationRepository.findAll(Sort.by("id"));
    return engineerLastLocations.stream()
        .map(engineerLastLocation -> mapToDTO(engineerLastLocation, new EngineerLastLocationDTO()))
        .toList();
  }

  public EngineerLastLocationDTO get(final Long id) {
    return engineerLastLocationRepository
        .findById(id)
        .map(engineerLastLocation -> mapToDTO(engineerLastLocation, new EngineerLastLocationDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public Long create(final EngineerLastLocationDTO engineerLastLocationDTO) {
    final EngineerLastLocation engineerLastLocation = new EngineerLastLocation();
    mapToEntity(engineerLastLocationDTO, engineerLastLocation);
    return engineerLastLocationRepository.save(engineerLastLocation).getId();
  }

  public void update(final Long id, final EngineerLastLocationDTO engineerLastLocationDTO) {
    final EngineerLastLocation engineerLastLocation =
        engineerLastLocationRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(engineerLastLocationDTO, engineerLastLocation);
    engineerLastLocationRepository.save(engineerLastLocation);
  }

  public void delete(final Long id) {
    final EngineerLastLocation engineerLastLocation =
        engineerLastLocationRepository.findById(id).orElseThrow(NotFoundException::new);
    engineerLastLocationRepository.delete(engineerLastLocation);
  }

  private EngineerLastLocationDTO mapToDTO(
      final EngineerLastLocation engineerLastLocation,
      final EngineerLastLocationDTO engineerLastLocationDTO) {
    engineerLastLocationDTO.setId(engineerLastLocation.getId());
    engineerLastLocationDTO.setAccuracyMeters(engineerLastLocation.getAccuracyMeters());
    engineerLastLocationDTO.setDateCreated(engineerLastLocation.getDateCreated());
    engineerLastLocationDTO.setLastUpdated(engineerLastLocation.getLastUpdated());
    engineerLastLocationDTO.setLat(engineerLastLocation.getLat());
    engineerLastLocationDTO.setLng(engineerLastLocation.getLng());
    engineerLastLocationDTO.setRecordedAt(engineerLastLocation.getRecordedAt());
    engineerLastLocationDTO.setUpdatedAt(engineerLastLocation.getUpdatedAt());
    engineerLastLocationDTO.setEngineerUserId(engineerLastLocation.getEngineerUserId());
    engineerLastLocationDTO.setOrganization(
        engineerLastLocation.getOrganization() == null
            ? null
            : engineerLastLocation.getOrganization().getId());
    return engineerLastLocationDTO;
  }

  private EngineerLastLocation mapToEntity(
      final EngineerLastLocationDTO engineerLastLocationDTO,
      final EngineerLastLocation engineerLastLocation) {
    engineerLastLocation.setAccuracyMeters(engineerLastLocationDTO.getAccuracyMeters());
    engineerLastLocation.setDateCreated(engineerLastLocationDTO.getDateCreated());
    engineerLastLocation.setLastUpdated(engineerLastLocationDTO.getLastUpdated());
    engineerLastLocation.setLat(engineerLastLocationDTO.getLat());
    engineerLastLocation.setLng(engineerLastLocationDTO.getLng());
    engineerLastLocation.setRecordedAt(engineerLastLocationDTO.getRecordedAt());
    engineerLastLocation.setUpdatedAt(engineerLastLocationDTO.getUpdatedAt());
    engineerLastLocation.setEngineerUserId(engineerLastLocationDTO.getEngineerUserId());
    final Organization organization =
        engineerLastLocationDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(engineerLastLocationDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    engineerLastLocation.setOrganization(organization);
    return engineerLastLocation;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final EngineerLastLocation organizationEngineerLastLocation =
        engineerLastLocationRepository.findFirstByOrganizationId(event.getId());
    if (organizationEngineerLastLocation != null) {
      referencedException.setKey("organization.engineerLastLocation.organization.referenced");
      referencedException.addParam(organizationEngineerLastLocation.getId());
      throw referencedException;
    }
  }
}
