package com.fieldops.fieldops_api.device_sync_state.rest;

import com.fieldops.fieldops_api.device_sync_state.model.DeviceSyncStateDTO;
import com.fieldops.fieldops_api.device_sync_state.service.DeviceSyncStateService;
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
@RequestMapping(value = "/api/deviceSyncStates", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceSyncStateResource {

    private final DeviceSyncStateService deviceSyncStateService;

    public DeviceSyncStateResource(final DeviceSyncStateService deviceSyncStateService) {
        this.deviceSyncStateService = deviceSyncStateService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceSyncStateDTO>> getAllDeviceSyncStates() {
        return ResponseEntity.ok(deviceSyncStateService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceSyncStateDTO> getDeviceSyncState(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(deviceSyncStateService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createDeviceSyncState(
            @RequestBody @Valid final DeviceSyncStateDTO deviceSyncStateDTO) {
        final UUID createdId = deviceSyncStateService.create(deviceSyncStateDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateDeviceSyncState(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final DeviceSyncStateDTO deviceSyncStateDTO) {
        deviceSyncStateService.update(id, deviceSyncStateDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceSyncState(@PathVariable(name = "id") final UUID id) {
        deviceSyncStateService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
