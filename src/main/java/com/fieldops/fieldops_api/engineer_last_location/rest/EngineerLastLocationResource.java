package com.fieldops.fieldops_api.engineer_last_location.rest;

import com.fieldops.fieldops_api.engineer_last_location.model.EngineerLastLocationDTO;
import com.fieldops.fieldops_api.engineer_last_location.service.EngineerLastLocationService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping(value = "/api/engineerLastLocations", produces = MediaType.APPLICATION_JSON_VALUE)
public class EngineerLastLocationResource {

    private final EngineerLastLocationService engineerLastLocationService;

    public EngineerLastLocationResource(
            final EngineerLastLocationService engineerLastLocationService) {
        this.engineerLastLocationService = engineerLastLocationService;
    }

    @GetMapping
    public ResponseEntity<List<EngineerLastLocationDTO>> getAllEngineerLastLocations() {
        return ResponseEntity.ok(engineerLastLocationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EngineerLastLocationDTO> getEngineerLastLocation(
            @PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(engineerLastLocationService.get(id));
    }

    @PostMapping
    public ResponseEntity<Long> createEngineerLastLocation(
            @RequestBody @Valid final EngineerLastLocationDTO engineerLastLocationDTO) {
        final Long createdId = engineerLastLocationService.create(engineerLastLocationDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateEngineerLastLocation(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final EngineerLastLocationDTO engineerLastLocationDTO) {
        engineerLastLocationService.update(id, engineerLastLocationDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEngineerLastLocation(
            @PathVariable(name = "id") final Long id) {
        engineerLastLocationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
