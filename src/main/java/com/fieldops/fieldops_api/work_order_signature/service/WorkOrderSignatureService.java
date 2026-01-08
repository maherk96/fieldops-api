package com.fieldops.fieldops_api.work_order_signature.service;

import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrder;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
import com.fieldops.fieldops_api.work_order_signature.domain.WorkOrderSignature;
import com.fieldops.fieldops_api.work_order_signature.model.WorkOrderSignatureDTO;
import com.fieldops.fieldops_api.work_order_signature.repos.WorkOrderSignatureRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderSignatureService {

  private final WorkOrderSignatureRepository workOrderSignatureRepository;
  private final WorkOrderRepository workOrderRepository;

  public WorkOrderSignatureService(
      final WorkOrderSignatureRepository workOrderSignatureRepository,
      final WorkOrderRepository workOrderRepository) {
    this.workOrderSignatureRepository = workOrderSignatureRepository;
    this.workOrderRepository = workOrderRepository;
  }

  public List<WorkOrderSignatureDTO> findAll() {
    final List<WorkOrderSignature> workOrderSignatures =
        workOrderSignatureRepository.findAll(Sort.by("id"));
    return workOrderSignatures.stream()
        .map(workOrderSignature -> mapToDTO(workOrderSignature, new WorkOrderSignatureDTO()))
        .toList();
  }

  public WorkOrderSignatureDTO get(final UUID id) {
    return workOrderSignatureRepository
        .findById(id)
        .map(workOrderSignature -> mapToDTO(workOrderSignature, new WorkOrderSignatureDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final WorkOrderSignatureDTO workOrderSignatureDTO) {
    final WorkOrderSignature workOrderSignature = new WorkOrderSignature();
    mapToEntity(workOrderSignatureDTO, workOrderSignature);
    return workOrderSignatureRepository.save(workOrderSignature).getId();
  }

  public void update(final UUID id, final WorkOrderSignatureDTO workOrderSignatureDTO) {
    final WorkOrderSignature workOrderSignature =
        workOrderSignatureRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(workOrderSignatureDTO, workOrderSignature);
    workOrderSignatureRepository.save(workOrderSignature);
  }

  public void delete(final UUID id) {
    final WorkOrderSignature workOrderSignature =
        workOrderSignatureRepository.findById(id).orElseThrow(NotFoundException::new);
    workOrderSignatureRepository.delete(workOrderSignature);
  }

  private WorkOrderSignatureDTO mapToDTO(
      final WorkOrderSignature workOrderSignature,
      final WorkOrderSignatureDTO workOrderSignatureDTO) {
    workOrderSignatureDTO.setId(workOrderSignature.getId());
    workOrderSignatureDTO.setSignatureType(workOrderSignature.getSignatureType());
    workOrderSignatureDTO.setSignerName(workOrderSignature.getSignerName());
    workOrderSignatureDTO.setStorageKey(workOrderSignature.getStorageKey());
    workOrderSignatureDTO.setSignedAt(workOrderSignature.getSignedAt());
    workOrderSignatureDTO.setCreatedAt(workOrderSignature.getCreatedAt());
    workOrderSignatureDTO.setWorkOrder(
        workOrderSignature.getWorkOrder() == null
            ? null
            : workOrderSignature.getWorkOrder().getId());
    return workOrderSignatureDTO;
  }

  private WorkOrderSignature mapToEntity(
      final WorkOrderSignatureDTO workOrderSignatureDTO,
      final WorkOrderSignature workOrderSignature) {
    workOrderSignature.setSignatureType(workOrderSignatureDTO.getSignatureType());
    workOrderSignature.setSignerName(workOrderSignatureDTO.getSignerName());
    workOrderSignature.setStorageKey(workOrderSignatureDTO.getStorageKey());
    workOrderSignature.setSignedAt(workOrderSignatureDTO.getSignedAt());
    workOrderSignature.setCreatedAt(workOrderSignatureDTO.getCreatedAt());
    final WorkOrder workOrder =
        workOrderSignatureDTO.getWorkOrder() == null
            ? null
            : workOrderRepository
                .findById(workOrderSignatureDTO.getWorkOrder())
                .orElseThrow(() -> new NotFoundException("workOrder not found"));
    workOrderSignature.setWorkOrder(workOrder);
    return workOrderSignature;
  }

  @EventListener(BeforeDeleteWorkOrder.class)
  public void on(final BeforeDeleteWorkOrder event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderSignature workOrderWorkOrderSignature =
        workOrderSignatureRepository.findFirstByWorkOrderId(event.getId());
    if (workOrderWorkOrderSignature != null) {
      referencedException.setKey("workOrder.workOrderSignature.workOrder.referenced");
      referencedException.addParam(workOrderWorkOrderSignature.getId());
      throw referencedException;
    }
  }
}
