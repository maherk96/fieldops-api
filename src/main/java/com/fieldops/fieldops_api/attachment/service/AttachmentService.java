package com.fieldops.fieldops_api.attachment.service;

import com.fieldops.fieldops_api.attachment.domain.Attachment;
import com.fieldops.fieldops_api.attachment.model.AttachmentDTO;
import com.fieldops.fieldops_api.attachment.repos.AttachmentRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;
  private final OrganizationRepository organizationRepository;

  public AttachmentService(
      final AttachmentRepository attachmentRepository,
      final OrganizationRepository organizationRepository) {
    this.attachmentRepository = attachmentRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<AttachmentDTO> findAll() {
    final List<Attachment> attachments = attachmentRepository.findAll(Sort.by("id"));
    return attachments.stream()
        .map(attachment -> mapToDTO(attachment, new AttachmentDTO()))
        .toList();
  }

  public AttachmentDTO get(final UUID id) {
    return attachmentRepository
        .findById(id)
        .map(attachment -> mapToDTO(attachment, new AttachmentDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final AttachmentDTO attachmentDTO) {
    final Attachment attachment = new Attachment();
    mapToEntity(attachmentDTO, attachment);
    return attachmentRepository.save(attachment).getId();
  }

  public void update(final UUID id, final AttachmentDTO attachmentDTO) {
    final Attachment attachment =
        attachmentRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(attachmentDTO, attachment);
    attachmentRepository.save(attachment);
  }

  public void delete(final UUID id) {
    final Attachment attachment =
        attachmentRepository.findById(id).orElseThrow(NotFoundException::new);
    attachmentRepository.delete(attachment);
  }

  private AttachmentDTO mapToDTO(final Attachment attachment, final AttachmentDTO attachmentDTO) {
    attachmentDTO.setId(attachment.getId());
    attachmentDTO.setAttachmentType(attachment.getAttachmentType());
    attachmentDTO.setDateCreated(attachment.getDateCreated());
    attachmentDTO.setFileName(attachment.getFileName());
    attachmentDTO.setLastUpdated(attachment.getLastUpdated());
    attachmentDTO.setMimeType(attachment.getMimeType());
    attachmentDTO.setSizeBytes(attachment.getSizeBytes());
    attachmentDTO.setStorageKey(attachment.getStorageKey());
    attachmentDTO.setUploadStatus(attachment.getUploadStatus());
    attachmentDTO.setUploadedFromDevice(attachment.getUploadedFromDevice());
    attachmentDTO.setEventId(attachment.getEventId());
    attachmentDTO.setUploadedByUserId(attachment.getUploadedByUserId());
    attachmentDTO.setWorkOrderId(attachment.getWorkOrderId());
    attachmentDTO.setOrganization(
        attachment.getOrganization() == null ? null : attachment.getOrganization().getId());
    return attachmentDTO;
  }

  private Attachment mapToEntity(final AttachmentDTO attachmentDTO, final Attachment attachment) {
    attachment.setAttachmentType(attachmentDTO.getAttachmentType());
    attachment.setFileName(attachmentDTO.getFileName());
    attachment.setMimeType(attachmentDTO.getMimeType());
    attachment.setSizeBytes(attachmentDTO.getSizeBytes());
    attachment.setStorageKey(attachmentDTO.getStorageKey());
    attachment.setUploadStatus(attachmentDTO.getUploadStatus());
    attachment.setUploadedFromDevice(attachmentDTO.getUploadedFromDevice());
    attachment.setEventId(attachmentDTO.getEventId());
    attachment.setUploadedByUserId(attachmentDTO.getUploadedByUserId());
    attachment.setWorkOrderId(attachmentDTO.getWorkOrderId());
    final Organization organization =
        attachmentDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(attachmentDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    attachment.setOrganization(organization);
    return attachment;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final Attachment organizationAttachment =
        attachmentRepository.findFirstByOrganizationId(event.getId());
    if (organizationAttachment != null) {
      referencedException.setKey("organization.attachment.organization.referenced");
      referencedException.addParam(organizationAttachment.getId());
      throw referencedException;
    }
  }
}
