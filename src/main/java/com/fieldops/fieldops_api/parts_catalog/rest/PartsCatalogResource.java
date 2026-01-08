package com.fieldops.fieldops_api.parts_catalog.rest;

import com.fieldops.fieldops_api.parts_catalog.model.PartsCatalogDTO;
import com.fieldops.fieldops_api.parts_catalog.service.PartsCatalogService;
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
@RequestMapping(value = "/api/partsCatalogs", produces = MediaType.APPLICATION_JSON_VALUE)
public class PartsCatalogResource {

  private final PartsCatalogService partsCatalogService;

  public PartsCatalogResource(final PartsCatalogService partsCatalogService) {
    this.partsCatalogService = partsCatalogService;
  }

  @GetMapping
  public ResponseEntity<List<PartsCatalogDTO>> getAllPartsCatalogs() {
    return ResponseEntity.ok(partsCatalogService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PartsCatalogDTO> getPartsCatalog(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(partsCatalogService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createPartsCatalog(
      @RequestBody @Valid final PartsCatalogDTO partsCatalogDTO) {
    final UUID createdId = partsCatalogService.create(partsCatalogDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updatePartsCatalog(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final PartsCatalogDTO partsCatalogDTO) {
    partsCatalogService.update(id, partsCatalogDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePartsCatalog(@PathVariable(name = "id") final UUID id) {
    partsCatalogService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
