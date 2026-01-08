package com.fieldops.fieldops_api.work_order_signature.rest;

import com.fieldops.fieldops_api.work_order_signature.model.WorkOrderSignatureDTO;
import com.fieldops.fieldops_api.work_order_signature.service.WorkOrderSignatureService;
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
@RequestMapping(value = "/api/workOrderSignatures", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkOrderSignatureResource {

    private final WorkOrderSignatureService workOrderSignatureService;

    public WorkOrderSignatureResource(final WorkOrderSignatureService workOrderSignatureService) {
        this.workOrderSignatureService = workOrderSignatureService;
    }

    @GetMapping
    public ResponseEntity<List<WorkOrderSignatureDTO>> getAllWorkOrderSignatures() {
        return ResponseEntity.ok(workOrderSignatureService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderSignatureDTO> getWorkOrderSignature(
            @PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(workOrderSignatureService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createWorkOrderSignature(
            @RequestBody @Valid final WorkOrderSignatureDTO workOrderSignatureDTO) {
        final UUID createdId = workOrderSignatureService.create(workOrderSignatureDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateWorkOrderSignature(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final WorkOrderSignatureDTO workOrderSignatureDTO) {
        workOrderSignatureService.update(id, workOrderSignatureDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrderSignature(@PathVariable(name = "id") final UUID id) {
        workOrderSignatureService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
