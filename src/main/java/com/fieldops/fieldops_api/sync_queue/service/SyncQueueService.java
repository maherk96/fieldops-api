package com.fieldops.fieldops_api.sync_queue.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.sync_queue.domain.SyncQueue;
import com.fieldops.fieldops_api.sync_queue.model.SyncQueueDTO;
import com.fieldops.fieldops_api.sync_queue.repos.SyncQueueRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SyncQueueService {

  private final SyncQueueRepository syncQueueRepository;
  private final OrganizationRepository organizationRepository;

  public SyncQueueService(
      final SyncQueueRepository syncQueueRepository,
      final OrganizationRepository organizationRepository) {
    this.syncQueueRepository = syncQueueRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<SyncQueueDTO> findAll() {
    final List<SyncQueue> syncQueues = syncQueueRepository.findAll(Sort.by("id"));
    return syncQueues.stream().map(syncQueue -> mapToDTO(syncQueue, new SyncQueueDTO())).toList();
  }

  public SyncQueueDTO get(final UUID id) {
    return syncQueueRepository
        .findById(id)
        .map(syncQueue -> mapToDTO(syncQueue, new SyncQueueDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final SyncQueueDTO syncQueueDTO) {
    final SyncQueue syncQueue = new SyncQueue();
    mapToEntity(syncQueueDTO, syncQueue);
    return syncQueueRepository.save(syncQueue).getId();
  }

  public void update(final UUID id, final SyncQueueDTO syncQueueDTO) {
    final SyncQueue syncQueue =
        syncQueueRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(syncQueueDTO, syncQueue);
    syncQueueRepository.save(syncQueue);
  }

  public void delete(final UUID id) {
    final SyncQueue syncQueue =
        syncQueueRepository.findById(id).orElseThrow(NotFoundException::new);
    syncQueueRepository.delete(syncQueue);
  }

  private SyncQueueDTO mapToDTO(final SyncQueue syncQueue, final SyncQueueDTO syncQueueDTO) {
    syncQueueDTO.setId(syncQueue.getId());
    syncQueueDTO.setDateCreated(syncQueue.getDateCreated());
    syncQueueDTO.setDeviceId(syncQueue.getDeviceId());
    syncQueueDTO.setLastError(syncQueue.getLastError());
    syncQueueDTO.setLastUpdated(syncQueue.getLastUpdated());
    syncQueueDTO.setNextRetryAt(syncQueue.getNextRetryAt());
    syncQueueDTO.setOperationType(syncQueue.getOperationType());
    syncQueueDTO.setPayload(syncQueue.getPayload());
    syncQueueDTO.setRecordId(syncQueue.getRecordId());
    syncQueueDTO.setRetryCount(syncQueue.getRetryCount());
    syncQueueDTO.setStatus(syncQueue.getStatus());
    syncQueueDTO.setTableName(syncQueue.getTableName());
    syncQueueDTO.setUpdatedAt(syncQueue.getUpdatedAt());
    syncQueueDTO.setUserId(syncQueue.getUserId());
    syncQueueDTO.setOrganization(
        syncQueue.getOrganization() == null ? null : syncQueue.getOrganization().getId());
    return syncQueueDTO;
  }

  private SyncQueue mapToEntity(final SyncQueueDTO syncQueueDTO, final SyncQueue syncQueue) {

    syncQueue.setDeviceId(syncQueueDTO.getDeviceId());
    syncQueue.setLastError(syncQueueDTO.getLastError());
    syncQueue.setNextRetryAt(syncQueueDTO.getNextRetryAt());
    syncQueue.setOperationType(syncQueueDTO.getOperationType());
    syncQueue.setPayload(syncQueueDTO.getPayload());
    syncQueue.setRecordId(syncQueueDTO.getRecordId());
    syncQueue.setRetryCount(syncQueueDTO.getRetryCount());
    syncQueue.setStatus(syncQueueDTO.getStatus());
    syncQueue.setTableName(syncQueueDTO.getTableName());
    syncQueue.setUpdatedAt(syncQueueDTO.getUpdatedAt());
    syncQueue.setUserId(syncQueueDTO.getUserId());
    final Organization organization =
        syncQueueDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(syncQueueDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    syncQueue.setOrganization(organization);
    return syncQueue;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final SyncQueue organizationSyncQueue =
        syncQueueRepository.findFirstByOrganizationId(event.getId());
    if (organizationSyncQueue != null) {
      referencedException.setKey("organization.syncQueue.organization.referenced");
      referencedException.addParam(organizationSyncQueue.getId());
      throw referencedException;
    }
  }
}
