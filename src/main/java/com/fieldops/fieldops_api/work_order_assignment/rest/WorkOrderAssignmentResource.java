package com.fieldops.fieldops_api.work_order_assignment.rest;

import com.fieldops.fieldops_api.work_order_assignment.model.WorkOrderAssignmentDTO;
import com.fieldops.fieldops_api.work_order_assignment.service.WorkOrderAssignmentService;
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
@RequestMapping(value = "/api/workOrderAssignments", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkOrderAssignmentResource {

    private final WorkOrderAssignmentService workOrderAssignmentService;

    public WorkOrderAssignmentResource(
            final WorkOrderAssignmentService workOrderAssignmentService) {
        this.workOrderAssignmentService = workOrderAssignmentService;
    }

    @GetMapping
    public ResponseEntity<List<WorkOrderAssignmentDTO>> getAllWorkOrderAssignments() {
        return ResponseEntity.ok(workOrderAssignmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderAssignmentDTO> getWorkOrderAssignment(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(workOrderAssignmentService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createWorkOrderAssignment(
            @RequestBody @Valid final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
        final UUID createdId = workOrderAssignmentService.create(workOrderAssignmentDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateWorkOrderAssignment(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
        workOrderAssignmentService.update(id, workOrderAssignmentDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrderAssignment(
            @PathVariable(name = "id") final UUID id) {
        workOrderAssignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
