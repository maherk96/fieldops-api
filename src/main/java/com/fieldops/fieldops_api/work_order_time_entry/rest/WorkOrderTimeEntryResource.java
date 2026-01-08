package com.fieldops.fieldops_api.work_order_time_entry.rest;

import com.fieldops.fieldops_api.work_order_time_entry.model.WorkOrderTimeEntryDTO;
import com.fieldops.fieldops_api.work_order_time_entry.service.WorkOrderTimeEntryService;
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
@RequestMapping(value = "/api/workOrderTimeEntries", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkOrderTimeEntryResource {

  private final WorkOrderTimeEntryService workOrderTimeEntryService;

  public WorkOrderTimeEntryResource(final WorkOrderTimeEntryService workOrderTimeEntryService) {
    this.workOrderTimeEntryService = workOrderTimeEntryService;
  }

  @GetMapping
  public ResponseEntity<List<WorkOrderTimeEntryDTO>> getAllWorkOrderTimeEntries() {
    return ResponseEntity.ok(workOrderTimeEntryService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<WorkOrderTimeEntryDTO> getWorkOrderTimeEntry(
      @PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(workOrderTimeEntryService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createWorkOrderTimeEntry(
      @RequestBody @Valid final WorkOrderTimeEntryDTO workOrderTimeEntryDTO) {
    final UUID createdId = workOrderTimeEntryService.create(workOrderTimeEntryDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateWorkOrderTimeEntry(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final WorkOrderTimeEntryDTO workOrderTimeEntryDTO) {
    workOrderTimeEntryService.update(id, workOrderTimeEntryDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkOrderTimeEntry(@PathVariable(name = "id") final UUID id) {
    workOrderTimeEntryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
