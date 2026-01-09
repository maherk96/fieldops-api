package com.fieldops.fieldops_api.location.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.location.domain.Location;
import com.fieldops.fieldops_api.location.model.LocationDTO;
import com.fieldops.fieldops_api.location.repos.LocationRepository;
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
public class LocationService {

  private final LocationRepository locationRepository;
  private final OrganizationRepository organizationRepository;

  public LocationService(
      final LocationRepository locationRepository,
      final OrganizationRepository organizationRepository) {
    this.locationRepository = locationRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<LocationDTO> findAll() {
    final List<Location> locations = locationRepository.findAll(Sort.by("id"));
    return locations.stream().map(location -> mapToDTO(location, new LocationDTO())).toList();
  }

  public LocationDTO get(final UUID id) {
    return locationRepository
        .findById(id)
        .map(location -> mapToDTO(location, new LocationDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final LocationDTO locationDTO) {
    final Location location = new Location();
    mapToEntity(locationDTO, location);
    return locationRepository.save(location).getId();
  }

  public void update(final UUID id, final LocationDTO locationDTO) {
    final Location location = locationRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(locationDTO, location);
    locationRepository.save(location);
  }

  public void delete(final UUID id) {
    final Location location = locationRepository.findById(id).orElseThrow(NotFoundException::new);
    locationRepository.delete(location);
  }

  private LocationDTO mapToDTO(final Location location, final LocationDTO locationDTO) {
    locationDTO.setId(location.getId());
    locationDTO.setAddressLine1(location.getAddressLine1());
    locationDTO.setAddressLine2(location.getAddressLine2());
    locationDTO.setChangeVersion(location.getChangeVersion());
    locationDTO.setCity(location.getCity());
    locationDTO.setContactPhone(location.getContactPhone());
    locationDTO.setCountry(location.getCountry());
    locationDTO.setDateCreated(location.getDateCreated());
    locationDTO.setLastUpdated(location.getLastUpdated());
    locationDTO.setLat(location.getLat());
    locationDTO.setLng(location.getLng());
    locationDTO.setName(location.getName());
    locationDTO.setPostcode(location.getPostcode());
    locationDTO.setRegion(location.getRegion());
    locationDTO.setUpdatedAt(location.getUpdatedAt());
    locationDTO.setVersion(location.getVersion());
    locationDTO.setCustomerId(location.getCustomerId());
    locationDTO.setOrganization(
        location.getOrganization() == null ? null : location.getOrganization().getId());
    return locationDTO;
  }

  private Location mapToEntity(final LocationDTO locationDTO, final Location location) {
    location.setAddressLine1(locationDTO.getAddressLine1());
    location.setAddressLine2(locationDTO.getAddressLine2());
    location.setChangeVersion(locationDTO.getChangeVersion());
    location.setCity(locationDTO.getCity());
    location.setContactPhone(locationDTO.getContactPhone());
    location.setCountry(locationDTO.getCountry());
    location.setLat(locationDTO.getLat());
    location.setLng(locationDTO.getLng());
    location.setName(locationDTO.getName());
    location.setPostcode(locationDTO.getPostcode());
    location.setRegion(locationDTO.getRegion());
    location.setUpdatedAt(locationDTO.getUpdatedAt());
    location.setVersion(locationDTO.getVersion());
    location.setCustomerId(locationDTO.getCustomerId());
    final Organization organization =
        locationDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(locationDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    location.setOrganization(organization);
    return location;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final Location organizationLocation =
        locationRepository.findFirstByOrganizationId(event.getId());
    if (organizationLocation != null) {
      referencedException.setKey("organization.location.organization.referenced");
      referencedException.addParam(organizationLocation.getId());
      throw referencedException;
    }
  }
}
