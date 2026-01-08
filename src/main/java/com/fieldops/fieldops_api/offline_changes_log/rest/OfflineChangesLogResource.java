package com.fieldops.fieldops_api.offline_changes_log.rest;

import com.fieldops.fieldops_api.offline_changes_log.model.OfflineChangesLogDTO;
import com.fieldops.fieldops_api.offline_changes_log.service.OfflineChangesLogService;
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
@RequestMapping(value = "/api/offlineChangesLogs", produces = MediaType.APPLICATION_JSON_VALUE)
public class OfflineChangesLogResource {

    private final OfflineChangesLogService offlineChangesLogService;

    public OfflineChangesLogResource(final OfflineChangesLogService offlineChangesLogService) {
        this.offlineChangesLogService = offlineChangesLogService;
    }

    @GetMapping
    public ResponseEntity<List<OfflineChangesLogDTO>> getAllOfflineChangesLogs() {
        return ResponseEntity.ok(offlineChangesLogService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfflineChangesLogDTO> getOfflineChangesLog(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(offlineChangesLogService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createOfflineChangesLog(
            @RequestBody @Valid final OfflineChangesLogDTO offlineChangesLogDTO) {
        final UUID createdId = offlineChangesLogService.create(offlineChangesLogDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateOfflineChangesLog(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final OfflineChangesLogDTO offlineChangesLogDTO) {
        offlineChangesLogService.update(id, offlineChangesLogDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfflineChangesLog(@PathVariable(name = "id") final UUID id) {
        offlineChangesLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
