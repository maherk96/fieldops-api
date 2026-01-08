package com.fieldops.fieldops_api.attachment.service;

import com.fieldops.fieldops_api.attachment.domain.Attachment;
import com.fieldops.fieldops_api.attachment.model.AttachmentDTO;
import com.fieldops.fieldops_api.attachment.repos.AttachmentRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrder;
import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrderEvent;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import com.fieldops.fieldops_api.work_order_event.repos.WorkOrderEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;
  private final WorkOrderRepository workOrderRepository;
  private final WorkOrderEventRepository workOrderEventRepository;
  private final UserRepository userRepository;

  public AttachmentService(
      final AttachmentRepository attachmentRepository,
      final WorkOrderRepository workOrderRepository,
      final WorkOrderEventRepository workOrderEventRepository,
      final UserRepository userRepository) {
    this.attachmentRepository = attachmentRepository;
    this.workOrderRepository = workOrderRepository;
    this.workOrderEventRepository = workOrderEventRepository;
    this.userRepository = userRepository;
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
    attachmentDTO.setStorageKey(attachment.getStorageKey());
    attachmentDTO.setFileName(attachment.getFileName());
    attachmentDTO.setMimeType(attachment.getMimeType());
    attachmentDTO.setSizeBytes(attachment.getSizeBytes());
    attachmentDTO.setUploadStatus(attachment.getUploadStatus());
    attachmentDTO.setUploadedFromDevice(attachment.getUploadedFromDevice());
    attachmentDTO.setCreatedAt(attachment.getCreatedAt());
    attachmentDTO.setWorkOrder(
        attachment.getWorkOrder() == null ? null : attachment.getWorkOrder().getId());
    attachmentDTO.setEvent(attachment.getEvent() == null ? null : attachment.getEvent().getId());
    attachmentDTO.setUploadedByUser(
        attachment.getUploadedByUser() == null ? null : attachment.getUploadedByUser().getId());
    return attachmentDTO;
  }

  private Attachment mapToEntity(final AttachmentDTO attachmentDTO, final Attachment attachment) {
    attachment.setAttachmentType(attachmentDTO.getAttachmentType());
    attachment.setStorageKey(attachmentDTO.getStorageKey());
    attachment.setFileName(attachmentDTO.getFileName());
    attachment.setMimeType(attachmentDTO.getMimeType());
    attachment.setSizeBytes(attachmentDTO.getSizeBytes());
    attachment.setUploadStatus(attachmentDTO.getUploadStatus());
    attachment.setUploadedFromDevice(attachmentDTO.getUploadedFromDevice());
    attachment.setCreatedAt(attachmentDTO.getCreatedAt());
    final WorkOrder workOrder =
        attachmentDTO.getWorkOrder() == null
            ? null
            : workOrderRepository
                .findById(attachmentDTO.getWorkOrder())
                .orElseThrow(() -> new NotFoundException("workOrder not found"));
    attachment.setWorkOrder(workOrder);
    final WorkOrderEvent event =
        attachmentDTO.getEvent() == null
            ? null
            : workOrderEventRepository
                .findById(attachmentDTO.getEvent())
                .orElseThrow(() -> new NotFoundException("event not found"));
    attachment.setEvent(event);
    final User uploadedByUser =
        attachmentDTO.getUploadedByUser() == null
            ? null
            : userRepository
                .findById(attachmentDTO.getUploadedByUser())
                .orElseThrow(() -> new NotFoundException("uploadedByUser not found"));
    attachment.setUploadedByUser(uploadedByUser);
    return attachment;
  }

  @EventListener(BeforeDeleteWorkOrder.class)
  public void on(final BeforeDeleteWorkOrder event) {
    final ReferencedException referencedException = new ReferencedException();
    final Attachment workOrderAttachment =
        attachmentRepository.findFirstByWorkOrderId(event.getId());
    if (workOrderAttachment != null) {
      referencedException.setKey("workOrder.attachment.workOrder.referenced");
      referencedException.addParam(workOrderAttachment.getId());
      throw referencedException;
    }
  }

  @EventListener(BeforeDeleteWorkOrderEvent.class)
  public void on(final BeforeDeleteWorkOrderEvent event) {
    final ReferencedException referencedException = new ReferencedException();
    final Attachment eventAttachment = attachmentRepository.findFirstByEventId(event.getId());
    if (eventAttachment != null) {
      referencedException.setKey("workOrderEvent.attachment.event.referenced");
      referencedException.addParam(eventAttachment.getId());
      throw referencedException;
    }
  }

  @EventListener(BeforeDeleteUser.class)
  public void on(final BeforeDeleteUser event) {
    final ReferencedException referencedException = new ReferencedException();
    final Attachment uploadedByUserAttachment =
        attachmentRepository.findFirstByUploadedByUserId(event.getId());
    if (uploadedByUserAttachment != null) {
      referencedException.setKey("user.attachment.uploadedByUser.referenced");
      referencedException.addParam(uploadedByUserAttachment.getId());
      throw referencedException;
    }
  }
}
