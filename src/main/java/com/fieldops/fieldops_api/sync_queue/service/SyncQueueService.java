package com.fieldops.fieldops_api.sync_queue.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.sync_queue.domain.SyncQueue;
import com.fieldops.fieldops_api.sync_queue.model.SyncQueueDTO;
import com.fieldops.fieldops_api.sync_queue.repos.SyncQueueRepository;
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
public class SyncQueueService {

    private final SyncQueueRepository syncQueueRepository;
    private final UserRepository userRepository;

    public SyncQueueService(final SyncQueueRepository syncQueueRepository,
            final UserRepository userRepository) {
        this.syncQueueRepository = syncQueueRepository;
        this.userRepository = userRepository;
    }

    public List<SyncQueueDTO> findAll() {
        final List<SyncQueue> syncQueues = syncQueueRepository.findAll(Sort.by("id"));
        return syncQueues.stream()
                .map(syncQueue -> mapToDTO(syncQueue, new SyncQueueDTO()))
                .toList();
    }

    public SyncQueueDTO get(final UUID id) {
        return syncQueueRepository.findById(id)
                .map(syncQueue -> mapToDTO(syncQueue, new SyncQueueDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final SyncQueueDTO syncQueueDTO) {
        final SyncQueue syncQueue = new SyncQueue();
        mapToEntity(syncQueueDTO, syncQueue);
        return syncQueueRepository.save(syncQueue).getId();
    }

    public void update(final UUID id, final SyncQueueDTO syncQueueDTO) {
        final SyncQueue syncQueue = syncQueueRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(syncQueueDTO, syncQueue);
        syncQueueRepository.save(syncQueue);
    }

    public void delete(final UUID id) {
        final SyncQueue syncQueue = syncQueueRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        syncQueueRepository.delete(syncQueue);
    }

    private SyncQueueDTO mapToDTO(final SyncQueue syncQueue, final SyncQueueDTO syncQueueDTO) {
        syncQueueDTO.setId(syncQueue.getId());
        syncQueueDTO.setDeviceId(syncQueue.getDeviceId());
        syncQueueDTO.setOperationType(syncQueue.getOperationType());
        syncQueueDTO.setTableName(syncQueue.getTableName());
        syncQueueDTO.setRecordId(syncQueue.getRecordId());
        syncQueueDTO.setPayload(syncQueue.getPayload());
        syncQueueDTO.setRetryCount(syncQueue.getRetryCount());
        syncQueueDTO.setLastError(syncQueue.getLastError());
        syncQueueDTO.setNextRetryAt(syncQueue.getNextRetryAt());
        syncQueueDTO.setStatus(syncQueue.getStatus());
        syncQueueDTO.setCreatedAt(syncQueue.getCreatedAt());
        syncQueueDTO.setUpdatedAt(syncQueue.getUpdatedAt());
        syncQueueDTO.setUser(syncQueue.getUser() == null ? null : syncQueue.getUser().getId());
        return syncQueueDTO;
    }

    private SyncQueue mapToEntity(final SyncQueueDTO syncQueueDTO, final SyncQueue syncQueue) {
        syncQueue.setDeviceId(syncQueueDTO.getDeviceId());
        syncQueue.setOperationType(syncQueueDTO.getOperationType());
        syncQueue.setTableName(syncQueueDTO.getTableName());
        syncQueue.setRecordId(syncQueueDTO.getRecordId());
        syncQueue.setPayload(syncQueueDTO.getPayload());
        syncQueue.setRetryCount(syncQueueDTO.getRetryCount());
        syncQueue.setLastError(syncQueueDTO.getLastError());
        syncQueue.setNextRetryAt(syncQueueDTO.getNextRetryAt());
        syncQueue.setStatus(syncQueueDTO.getStatus());
        syncQueue.setCreatedAt(syncQueueDTO.getCreatedAt());
        syncQueue.setUpdatedAt(syncQueueDTO.getUpdatedAt());
        final User user = syncQueueDTO.getUser() == null ? null : userRepository.findById(syncQueueDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        syncQueue.setUser(user);
        return syncQueue;
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final SyncQueue userSyncQueue = syncQueueRepository.findFirstByUserId(event.getId());
        if (userSyncQueue != null) {
            referencedException.setKey("user.syncQueue.user.referenced");
            referencedException.addParam(userSyncQueue.getId());
            throw referencedException;
        }
    }

}
