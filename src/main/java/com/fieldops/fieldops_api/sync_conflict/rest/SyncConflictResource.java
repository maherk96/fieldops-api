package com.fieldops.fieldops_api.sync_conflict.rest;

import com.fieldops.fieldops_api.sync_conflict.model.SyncConflictDTO;
import com.fieldops.fieldops_api.sync_conflict.service.SyncConflictService;
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
@RequestMapping(value = "/api/syncConflicts", produces = MediaType.APPLICATION_JSON_VALUE)
public class SyncConflictResource {

    private final SyncConflictService syncConflictService;

    public SyncConflictResource(final SyncConflictService syncConflictService) {
        this.syncConflictService = syncConflictService;
    }

    @GetMapping
    public ResponseEntity<List<SyncConflictDTO>> getAllSyncConflicts() {
        return ResponseEntity.ok(syncConflictService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyncConflictDTO> getSyncConflict(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(syncConflictService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createSyncConflict(
            @RequestBody @Valid final SyncConflictDTO syncConflictDTO) {
        final UUID createdId = syncConflictService.create(syncConflictDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateSyncConflict(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final SyncConflictDTO syncConflictDTO) {
        syncConflictService.update(id, syncConflictDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSyncConflict(@PathVariable(name = "id") final UUID id) {
        syncConflictService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
