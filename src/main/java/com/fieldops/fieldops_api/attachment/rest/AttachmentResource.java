package com.fieldops.fieldops_api.attachment.rest;

import com.fieldops.fieldops_api.attachment.model.AttachmentDTO;
import com.fieldops.fieldops_api.attachment.service.AttachmentService;
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
@RequestMapping(value = "/api/attachments", produces = MediaType.APPLICATION_JSON_VALUE)
public class AttachmentResource {

  private final AttachmentService attachmentService;

  public AttachmentResource(final AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @GetMapping
  public ResponseEntity<List<AttachmentDTO>> getAllAttachments() {
    return ResponseEntity.ok(attachmentService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AttachmentDTO> getAttachment(@PathVariable(name = "id") final UUID id) {
    return ResponseEntity.ok(attachmentService.get(id));
  }

  @PostMapping
  public ResponseEntity<UUID> createAttachment(
      @RequestBody @Valid final AttachmentDTO attachmentDTO) {
    final UUID createdId = attachmentService.create(attachmentDTO);
    return new ResponseEntity<>(createdId, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UUID> updateAttachment(
      @PathVariable(name = "id") final UUID id,
      @RequestBody @Valid final AttachmentDTO attachmentDTO) {
    attachmentService.update(id, attachmentDTO);
    return ResponseEntity.ok(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAttachment(@PathVariable(name = "id") final UUID id) {
    attachmentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
