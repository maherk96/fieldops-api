package com.fieldops.fieldops_api.audit_log.rest;

import com.fieldops.fieldops_api.audit_log.model.AuditLogDTO;
import com.fieldops.fieldops_api.audit_log.service.AuditLogService;
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
@RequestMapping(value = "/api/auditLogs", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuditLogResource {

  private final AuditLogService auditLogService;

  public AuditLogResource(final AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs() {
    return ResponseEntity.ok(auditLogService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(auditLogService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createAuditLog(@RequestBody @Valid final AuditLogDTO auditLogDTO) {
    final UUID createdId = auditLogService.create(auditLogDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateAuditLog(
      @PathVariable(name = "id") final UUID id, @RequestBody @Valid final AuditLogDTO auditLogDTO) {
    auditLogService.update(id, auditLogDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAuditLog(@PathVariable(name = "id") final UUID id) {
    auditLogService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
