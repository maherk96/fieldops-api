package com.fieldops.fieldops_api.sync_conflict.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.sync_conflict.domain.SyncConflict;
import com.fieldops.fieldops_api.sync_conflict.model.SyncConflictDTO;
import com.fieldops.fieldops_api.sync_conflict.repos.SyncConflictRepository;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
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
  private final UserRepository userRepository;

  public SyncConflictService(
      final SyncConflictRepository syncConflictRepository, final UserRepository userRepository) {
    this.syncConflictRepository = syncConflictRepository;
    this.userRepository = userRepository;
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
    syncConflictDTO.setDeviceId(syncConflict.getDeviceId());
    syncConflictDTO.setTableName(syncConflict.getTableName());
    syncConflictDTO.setRecordId(syncConflict.getRecordId());
    syncConflictDTO.setConflictType(syncConflict.getConflictType());
    syncConflictDTO.setLocalVersion(syncConflict.getLocalVersion());
    syncConflictDTO.setServerVersion(syncConflict.getServerVersion());
    syncConflictDTO.setLocalData(syncConflict.getLocalData());
    syncConflictDTO.setServerData(syncConflict.getServerData());
    syncConflictDTO.setResolved(syncConflict.getResolved());
    syncConflictDTO.setResolutionStrategy(syncConflict.getResolutionStrategy());
    syncConflictDTO.setResolvedAt(syncConflict.getResolvedAt());
    syncConflictDTO.setCreatedAt(syncConflict.getCreatedAt());
    syncConflictDTO.setResolvedByUser(
        syncConflict.getResolvedByUser() == null ? null : syncConflict.getResolvedByUser().getId());
    return syncConflictDTO;
  }

  private SyncConflict mapToEntity(
      final SyncConflictDTO syncConflictDTO, final SyncConflict syncConflict) {
    syncConflict.setDeviceId(syncConflictDTO.getDeviceId());
    syncConflict.setTableName(syncConflictDTO.getTableName());
    syncConflict.setRecordId(syncConflictDTO.getRecordId());
    syncConflict.setConflictType(syncConflictDTO.getConflictType());
    syncConflict.setLocalVersion(syncConflictDTO.getLocalVersion());
    syncConflict.setServerVersion(syncConflictDTO.getServerVersion());
    syncConflict.setLocalData(syncConflictDTO.getLocalData());
    syncConflict.setServerData(syncConflictDTO.getServerData());
    syncConflict.setResolved(syncConflictDTO.getResolved());
    syncConflict.setResolutionStrategy(syncConflictDTO.getResolutionStrategy());
    syncConflict.setResolvedAt(syncConflictDTO.getResolvedAt());
    syncConflict.setCreatedAt(syncConflictDTO.getCreatedAt());
    final User resolvedByUser =
        syncConflictDTO.getResolvedByUser() == null
            ? null
            : userRepository
                .findById(syncConflictDTO.getResolvedByUser())
                .orElseThrow(() -> new NotFoundException("resolvedByUser not found"));
    syncConflict.setResolvedByUser(resolvedByUser);
    return syncConflict;
  }

  @EventListener(BeforeDeleteUser.class)
  public void on(final BeforeDeleteUser event) {
    final ReferencedException referencedException = new ReferencedException();
    final SyncConflict resolvedByUserSyncConflict =
        syncConflictRepository.findFirstByResolvedByUserId(event.getId());
    if (resolvedByUserSyncConflict != null) {
      referencedException.setKey("user.syncConflict.resolvedByUser.referenced");
      referencedException.addParam(resolvedByUserSyncConflict.getId());
      throw referencedException;
    }
  }
}
