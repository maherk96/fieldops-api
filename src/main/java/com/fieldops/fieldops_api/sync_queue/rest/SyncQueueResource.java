package com.fieldops.fieldops_api.sync_queue.rest;

import com.fieldops.fieldops_api.sync_queue.model.SyncQueueDTO;
import com.fieldops.fieldops_api.sync_queue.service.SyncQueueService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/syncQueues", produces = MediaType.APPLICATION_JSON_VALUE)
public class SyncQueueResource {

    private final SyncQueueService syncQueueService;

    public SyncQueueResource(final SyncQueueService syncQueueService) {
        this.syncQueueService = syncQueueService;
    }

    @GetMapping
    public ResponseEntity<List<SyncQueueDTO>> getAllSyncQueues() {
        return ResponseEntity.ok(syncQueueService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyncQueueDTO> getSyncQueue(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(syncQueueService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createSyncQueue(
            @RequestBody @Valid final SyncQueueDTO syncQueueDTO) {
        final UUID createdId = syncQueueService.create(syncQueueDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateSyncQueue(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final SyncQueueDTO syncQueueDTO) {
        syncQueueService.update(id, syncQueueDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSyncQueue(@PathVariable(name = "id") final UUID id) {
        syncQueueService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
