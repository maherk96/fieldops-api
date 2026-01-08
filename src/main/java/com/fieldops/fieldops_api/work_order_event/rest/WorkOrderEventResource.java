package com.fieldops.fieldops_api.work_order_event.rest;

import com.fieldops.fieldops_api.work_order_event.model.WorkOrderEventDTO;
import com.fieldops.fieldops_api.work_order_event.service.WorkOrderEventService;
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
@RequestMapping(value = "/api/workOrderEvents", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkOrderEventResource {

    private final WorkOrderEventService workOrderEventService;

    public WorkOrderEventResource(final WorkOrderEventService workOrderEventService) {
        this.workOrderEventService = workOrderEventService;
    }

    @GetMapping
    public ResponseEntity<List<WorkOrderEventDTO>> getAllWorkOrderEvents() {
        return ResponseEntity.ok(workOrderEventService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderEventDTO> getWorkOrderEvent(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(workOrderEventService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createWorkOrderEvent(
            @RequestBody @Valid final WorkOrderEventDTO workOrderEventDTO) {
        final UUID createdId = workOrderEventService.create(workOrderEventDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateWorkOrderEvent(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final WorkOrderEventDTO workOrderEventDTO) {
        workOrderEventService.update(id, workOrderEventDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrderEvent(@PathVariable(name = "id") final UUID id) {
        workOrderEventService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
