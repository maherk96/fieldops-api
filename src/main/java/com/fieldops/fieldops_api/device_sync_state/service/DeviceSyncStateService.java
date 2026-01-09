package com.fieldops.fieldops_api.device_sync_state.service;

import com.fieldops.fieldops_api.device_sync_state.domain.DeviceSyncState;
import com.fieldops.fieldops_api.device_sync_state.model.DeviceSyncStateDTO;
import com.fieldops.fieldops_api.device_sync_state.repos.DeviceSyncStateRepository;
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
public class DeviceSyncStateService {

  private final DeviceSyncStateRepository deviceSyncStateRepository;
  private final OrganizationRepository organizationRepository;

  public DeviceSyncStateService(
      final DeviceSyncStateRepository deviceSyncStateRepository,
      final OrganizationRepository organizationRepository) {
    this.deviceSyncStateRepository = deviceSyncStateRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<DeviceSyncStateDTO> findAll() {
    final List<DeviceSyncState> deviceSyncStates = deviceSyncStateRepository.findAll(Sort.by("id"));
    return deviceSyncStates.stream()
        .map(deviceSyncState -> mapToDTO(deviceSyncState, new DeviceSyncStateDTO()))
        .toList();
  }

  public DeviceSyncStateDTO get(final UUID id) {
    return deviceSyncStateRepository
        .findById(id)
        .map(deviceSyncState -> mapToDTO(deviceSyncState, new DeviceSyncStateDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final DeviceSyncStateDTO deviceSyncStateDTO) {
    final DeviceSyncState deviceSyncState = new DeviceSyncState();
    mapToEntity(deviceSyncStateDTO, deviceSyncState);
    return deviceSyncStateRepository.save(deviceSyncState).getId();
  }

  public void update(final UUID id, final DeviceSyncStateDTO deviceSyncStateDTO) {
    final DeviceSyncState deviceSyncState =
        deviceSyncStateRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(deviceSyncStateDTO, deviceSyncState);
    deviceSyncStateRepository.save(deviceSyncState);
  }

  public void delete(final UUID id) {
    final DeviceSyncState deviceSyncState =
        deviceSyncStateRepository.findById(id).orElseThrow(NotFoundException::new);
    deviceSyncStateRepository.delete(deviceSyncState);
  }

  private DeviceSyncStateDTO mapToDTO(
      final DeviceSyncState deviceSyncState, final DeviceSyncStateDTO deviceSyncStateDTO) {
    deviceSyncStateDTO.setId(deviceSyncState.getId());
    deviceSyncStateDTO.setDateCreated(deviceSyncState.getDateCreated());
    deviceSyncStateDTO.setDeviceId(deviceSyncState.getDeviceId());
    deviceSyncStateDTO.setLastChangeVersion(deviceSyncState.getLastChangeVersion());
    deviceSyncStateDTO.setLastSyncAt(deviceSyncState.getLastSyncAt());
    deviceSyncStateDTO.setLastUpdated(deviceSyncState.getLastUpdated());
    deviceSyncStateDTO.setTableName(deviceSyncState.getTableName());
    deviceSyncStateDTO.setUpdatedAt(deviceSyncState.getUpdatedAt());
    deviceSyncStateDTO.setUserId(deviceSyncState.getUserId());
    deviceSyncStateDTO.setOrganization(
        deviceSyncState.getOrganization() == null
            ? null
            : deviceSyncState.getOrganization().getId());
    return deviceSyncStateDTO;
  }

  private DeviceSyncState mapToEntity(
      final DeviceSyncStateDTO deviceSyncStateDTO, final DeviceSyncState deviceSyncState) {
    deviceSyncState.setDeviceId(deviceSyncStateDTO.getDeviceId());
    deviceSyncState.setLastChangeVersion(deviceSyncStateDTO.getLastChangeVersion());
    deviceSyncState.setLastSyncAt(deviceSyncStateDTO.getLastSyncAt());
    deviceSyncState.setTableName(deviceSyncStateDTO.getTableName());
    deviceSyncState.setUpdatedAt(deviceSyncStateDTO.getUpdatedAt());
    deviceSyncState.setUserId(deviceSyncStateDTO.getUserId());
    final Organization organization =
        deviceSyncStateDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(deviceSyncStateDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    deviceSyncState.setOrganization(organization);
    return deviceSyncState;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final DeviceSyncState organizationDeviceSyncState =
        deviceSyncStateRepository.findFirstByOrganizationId(event.getId());
    if (organizationDeviceSyncState != null) {
      referencedException.setKey("organization.deviceSyncState.organization.referenced");
      referencedException.addParam(organizationDeviceSyncState.getId());
      throw referencedException;
    }
  }
}
