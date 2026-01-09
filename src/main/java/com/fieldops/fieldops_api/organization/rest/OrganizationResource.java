package com.fieldops.fieldops_api.organization.rest;

import com.fieldops.fieldops_api.organization.model.OrganizationDTO;
import com.fieldops.fieldops_api.organization.service.OrganizationService;
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
@RequestMapping(value = "/api/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationResource {

  private final OrganizationService organizationService;

  public OrganizationResource(final OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @GetMapping
  public ResponseEntity<List<OrganizationDTO>> getAllOrganizations() {
    return ResponseEntity.ok(organizationService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrganizationDTO> getOrganization(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(organizationService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createOrganization(
      @RequestBody @Valid final OrganizationDTO organizationDTO) {
    final UUID createdId = organizationService.create(organizationDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateOrganization(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final OrganizationDTO organizationDTO) {
    organizationService.update(id, organizationDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrganization(@PathVariable(name = "id") final UUID id) {
    organizationService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
