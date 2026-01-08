package com.fieldops.fieldops_api.asset.rest;

import com.fieldops.fieldops_api.asset.model.AssetDTO;
import com.fieldops.fieldops_api.asset.service.AssetService;
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
@RequestMapping(value = "/api/assets", produces = MediaType.APPLICATION_JSON_VALUE)
public class AssetResource {

  private final AssetService assetService;

  public AssetResource(final AssetService assetService) {
    this.assetService = assetService;
  }

  @GetMapping
  public ResponseEntity<List<AssetDTO>> getAllAssets() {
    return ResponseEntity.ok(assetService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AssetDTO> getAsset(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(assetService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createAsset(@RequestBody @Valid final AssetDTO assetDTO) {
    final UUID createdId = assetService.create(assetDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateAsset(
      @PathVariable(name = "id") final UUID id, @RequestBody @Valid final AssetDTO assetDTO) {
    assetService.update(id, assetDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAsset(@PathVariable(name = "id") final UUID id) {
    assetService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
