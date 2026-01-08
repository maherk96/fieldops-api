package com.fieldops.fieldops_api.work_order.rest;

import com.fieldops.fieldops_api.work_order.model.WorkOrderDTO;
import com.fieldops.fieldops_api.work_order.service.WorkOrderService;
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
@RequestMapping(value = "/api/workOrders", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkOrderResource {

  private final WorkOrderService workOrderService;

  public WorkOrderResource(final WorkOrderService workOrderService) {
    this.workOrderService = workOrderService;
  }

  @GetMapping
  public ResponseEntity<List<WorkOrderDTO>> getAllWorkOrders() {
    return ResponseEntity.ok(workOrderService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<WorkOrderDTO> getWorkOrder(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(workOrderService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createWorkOrder(@RequestBody @Valid final WorkOrderDTO workOrderDTO) {
    final UUID createdId = workOrderService.create(workOrderDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateWorkOrder(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final WorkOrderDTO workOrderDTO) {
    workOrderService.update(id, workOrderDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkOrder(@PathVariable(name = "id") final UUID id) {
    workOrderService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
