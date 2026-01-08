package com.fieldops.fieldops_api.engineer_location.rest;

import com.fieldops.fieldops_api.engineer_location.model.EngineerLocationDTO;
import com.fieldops.fieldops_api.engineer_location.service.EngineerLocationService;
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
@RequestMapping(value = "/api/engineerLocations", produces = MediaType.APPLICATION_JSON_VALUE)
public class EngineerLocationResource {

    private final EngineerLocationService engineerLocationService;

    public EngineerLocationResource(final EngineerLocationService engineerLocationService) {
        this.engineerLocationService = engineerLocationService;
    }

    @GetMapping
    public ResponseEntity<List<EngineerLocationDTO>> getAllEngineerLocations() {
        return ResponseEntity.ok(engineerLocationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EngineerLocationDTO> getEngineerLocation(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(engineerLocationService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createEngineerLocation(
            @RequestBody @Valid final EngineerLocationDTO engineerLocationDTO) {
        final UUID createdId = engineerLocationService.create(engineerLocationDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateEngineerLocation(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final EngineerLocationDTO engineerLocationDTO) {
        engineerLocationService.update(id, engineerLocationDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEngineerLocation(@PathVariable(name = "id") final UUID id) {
        engineerLocationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
