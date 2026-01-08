package com.fieldops.fieldops_api.work_order_part.rest;

import com.fieldops.fieldops_api.work_order_part.model.WorkOrderPartDTO;
import com.fieldops.fieldops_api.work_order_part.service.WorkOrderPartService;
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
@RequestMapping(value = "/api/workOrderParts", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkOrderPartResource {

  private final WorkOrderPartService workOrderPartService;

  public WorkOrderPartResource(final WorkOrderPartService workOrderPartService) {
    this.workOrderPartService = workOrderPartService;
  }

  @GetMapping
  public ResponseEntity<List<WorkOrderPartDTO>> getAllWorkOrderParts() {
    return ResponseEntity.ok(workOrderPartService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<WorkOrderPartDTO> getWorkOrderPart(
      @PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(workOrderPartService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createWorkOrderPart(
      @RequestBody @Valid final WorkOrderPartDTO workOrderPartDTO) {
    final UUID createdId = workOrderPartService.create(workOrderPartDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateWorkOrderPart(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final WorkOrderPartDTO workOrderPartDTO) {
    workOrderPartService.update(id, workOrderPartDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkOrderPart(@PathVariable(name = "id") final UUID id) {
    workOrderPartService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
