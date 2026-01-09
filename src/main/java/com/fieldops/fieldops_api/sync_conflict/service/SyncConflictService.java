package com.fieldops.fieldops_api.sync_conflict.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.sync_conflict.domain.SyncConflict;
import com.fieldops.fieldops_api.sync_conflict.model.SyncConflictDTO;
import com.fieldops.fieldops_api.sync_conflict.repos.SyncConflictRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SyncConflictService {

  private final SyncConflictRepository syncConflictRepository;
  private final OrganizationRepository organizationRepository;

  public SyncConflictService(
      final SyncConflictRepository syncConflictRepository,
      final OrganizationRepository organizationRepository) {
    this.syncConflictRepository = syncConflictRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<SyncConflictDTO> findAll() {
    final List<SyncConflict> syncConflicts = syncConflictRepository.findAll(Sort.by("id"));
    return syncConflicts.stream()
        .map(syncConflict -> mapToDTO(syncConflict, new SyncConflictDTO()))
        .toList();
  }

  public SyncConflictDTO get(final UUID id) {
    return syncConflictRepository
        .findById(id)
        .map(syncConflict -> mapToDTO(syncConflict, new SyncConflictDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final SyncConflictDTO syncConflictDTO) {
    final SyncConflict syncConflict = new SyncConflict();
    mapToEntity(syncConflictDTO, syncConflict);
    return syncConflictRepository.save(syncConflict).getId();
  }

  public void update(final UUID id, final SyncConflictDTO syncConflictDTO) {
    final SyncConflict syncConflict =
        syncConflictRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(syncConflictDTO, syncConflict);
    syncConflictRepository.save(syncConflict);
  }

  public void delete(final UUID id) {
    final SyncConflict syncConflict =
        syncConflictRepository.findById(id).orElseThrow(NotFoundException::new);
    syncConflictRepository.delete(syncConflict);
  }

  private SyncConflictDTO mapToDTO(
      final SyncConflict syncConflict, final SyncConflictDTO syncConflictDTO) {
    syncConflictDTO.setId(syncConflict.getId());
    syncConflictDTO.setConflictType(syncConflict.getConflictType());
    syncConflictDTO.setDateCreated(syncConflict.getDateCreated());
    syncConflictDTO.setDeviceId(syncConflict.getDeviceId());
    syncConflictDTO.setLastUpdated(syncConflict.getLastUpdated());
    syncConflictDTO.setLocalData(syncConflict.getLocalData());
    syncConflictDTO.setLocalVersion(syncConflict.getLocalVersion());
    syncConflictDTO.setRecordId(syncConflict.getRecordId());
    syncConflictDTO.setResolutionStrategy(syncConflict.getResolutionStrategy());
    syncConflictDTO.setResolved(syncConflict.getResolved());
    syncConflictDTO.setResolvedAt(syncConflict.getResolvedAt());
    syncConflictDTO.setServerData(syncConflict.getServerData());
    syncConflictDTO.setServerVersion(syncConflict.getServerVersion());
    syncConflictDTO.setTableName(syncConflict.getTableName());
    syncConflictDTO.setResolvedByUserId(syncConflict.getResolvedByUserId());
    syncConflictDTO.setOrganization(
        syncConflict.getOrganization() == null ? null : syncConflict.getOrganization().getId());
    return syncConflictDTO;
  }

  private SyncConflict mapToEntity(
      final SyncConflictDTO syncConflictDTO, final SyncConflict syncConflict) {
    syncConflict.setConflictType(syncConflictDTO.getConflictType());
    syncConflict.setDeviceId(syncConflictDTO.getDeviceId());
    syncConflict.setLocalData(syncConflictDTO.getLocalData());
    syncConflict.setLocalVersion(syncConflictDTO.getLocalVersion());
    syncConflict.setRecordId(syncConflictDTO.getRecordId());
    syncConflict.setResolutionStrategy(syncConflictDTO.getResolutionStrategy());
    syncConflict.setResolved(syncConflictDTO.getResolved());
    syncConflict.setResolvedAt(syncConflictDTO.getResolvedAt());
    syncConflict.setServerData(syncConflictDTO.getServerData());
    syncConflict.setServerVersion(syncConflictDTO.getServerVersion());
    syncConflict.setTableName(syncConflictDTO.getTableName());
    syncConflict.setResolvedByUserId(syncConflictDTO.getResolvedByUserId());
    final Organization organization =
        syncConflictDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(syncConflictDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    syncConflict.setOrganization(organization);
    return syncConflict;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final SyncConflict organizationSyncConflict =
        syncConflictRepository.findFirstByOrganizationId(event.getId());
    if (organizationSyncConflict != null) {
      referencedException.setKey("organization.syncConflict.organization.referenced");
      referencedException.addParam(organizationSyncConflict.getId());
      throw referencedException;
    }
  }
}
