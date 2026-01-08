package com.fieldops.fieldops_api.engineer_availability.rest;

import com.fieldops.fieldops_api.engineer_availability.model.EngineerAvailabilityDTO;
import com.fieldops.fieldops_api.engineer_availability.service.EngineerAvailabilityService;
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
@RequestMapping(value = "/api/engineerAvailabilities", produces = MediaType.APPLICATION_JSON_VALUE)
public class EngineerAvailabilityResource {

  private final EngineerAvailabilityService engineerAvailabilityService;

  public EngineerAvailabilityResource(
      final EngineerAvailabilityService engineerAvailabilityService) {
    this.engineerAvailabilityService = engineerAvailabilityService;
  }

  @GetMapping
  public ResponseEntity<List<EngineerAvailabilityDTO>> getAllEngineerAvailabilities() {
    return ResponseEntity.ok(engineerAvailabilityService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<EngineerAvailabilityDTO> getEngineerAvailability(
      @PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(engineerAvailabilityService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createEngineerAvailability(
      @RequestBody @Valid final EngineerAvailabilityDTO engineerAvailabilityDTO) {
    final UUID createdId = engineerAvailabilityService.create(engineerAvailabilityDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateEngineerAvailability(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final EngineerAvailabilityDTO engineerAvailabilityDTO) {
    engineerAvailabilityService.update(id, engineerAvailabilityDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEngineerAvailability(@PathVariable(name = "id") final UUID id) {
    engineerAvailabilityService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
