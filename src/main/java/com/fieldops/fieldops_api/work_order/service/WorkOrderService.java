package com.fieldops.fieldops_api.work_order.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.model.WorkOrderDTO;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderService {

  private final WorkOrderRepository workOrderRepository;
  private final OrganizationRepository organizationRepository;

  public WorkOrderService(
      final WorkOrderRepository workOrderRepository,
      final OrganizationRepository organizationRepository) {
    this.workOrderRepository = workOrderRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<WorkOrderDTO> findAll() {
    final List<WorkOrder> workOrders = workOrderRepository.findAll(Sort.by("id"));
    return workOrders.stream().map(workOrder -> mapToDTO(workOrder, new WorkOrderDTO())).toList();
  }

  public WorkOrderDTO get(final UUID id) {
    return workOrderRepository
        .findById(id)
        .map(workOrder -> mapToDTO(workOrder, new WorkOrderDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final WorkOrderDTO workOrderDTO) {
    final WorkOrder workOrder = new WorkOrder();
    mapToEntity(workOrderDTO, workOrder);
    return workOrderRepository.save(workOrder).getId();
  }

  public void update(final UUID id, final WorkOrderDTO workOrderDTO) {
    final WorkOrder workOrder =
        workOrderRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(workOrderDTO, workOrder);
    workOrderRepository.save(workOrder);
  }

  public void delete(final UUID id) {
    final WorkOrder workOrder =
        workOrderRepository.findById(id).orElseThrow(NotFoundException::new);
    workOrderRepository.delete(workOrder);
  }

  private WorkOrderDTO mapToDTO(final WorkOrder workOrder, final WorkOrderDTO workOrderDTO) {
    workOrderDTO.setId(workOrder.getId());
    workOrderDTO.setActualEnd(workOrder.getActualEnd());
    workOrderDTO.setActualStart(workOrder.getActualStart());
    workOrderDTO.setChangeVersion(workOrder.getChangeVersion());
    workOrderDTO.setCompletedAt(workOrder.getCompletedAt());
    workOrderDTO.setCompletionNotes(workOrder.getCompletionNotes());
    workOrderDTO.setDateCreated(workOrder.getDateCreated());
    workOrderDTO.setDeletedAt(workOrder.getDeletedAt());
    workOrderDTO.setDescription(workOrder.getDescription());
    workOrderDTO.setEstimatedDurationMinutes(workOrder.getEstimatedDurationMinutes());
    workOrderDTO.setLastUpdated(workOrder.getLastUpdated());
    workOrderDTO.setPriority(workOrder.getPriority());
    workOrderDTO.setScheduledEnd(workOrder.getScheduledEnd());
    workOrderDTO.setScheduledStart(workOrder.getScheduledStart());
    workOrderDTO.setStatus(workOrder.getStatus());
    workOrderDTO.setTitle(workOrder.getTitle());
    workOrderDTO.setVersion(workOrder.getVersion());
    workOrderDTO.setWorkOrderNo(workOrder.getWorkOrderNo());
    workOrderDTO.setAssetId(workOrder.getAssetId());
    workOrderDTO.setLastModifiedById(workOrder.getLastModifiedById());
    workOrderDTO.setLocationId(workOrder.getLocationId());
    workOrderDTO.setOrganization(
        workOrder.getOrganization() == null ? null : workOrder.getOrganization().getId());
    return workOrderDTO;
  }

  private WorkOrder mapToEntity(final WorkOrderDTO workOrderDTO, final WorkOrder workOrder) {
    workOrder.setActualEnd(workOrderDTO.getActualEnd());
    workOrder.setActualStart(workOrderDTO.getActualStart());
    workOrder.setChangeVersion(workOrderDTO.getChangeVersion());
    workOrder.setCompletedAt(workOrderDTO.getCompletedAt());
    workOrder.setCompletionNotes(workOrderDTO.getCompletionNotes());
    workOrder.setDateCreated(workOrderDTO.getDateCreated());
    workOrder.setDeletedAt(workOrderDTO.getDeletedAt());
    workOrder.setDescription(workOrderDTO.getDescription());
    workOrder.setEstimatedDurationMinutes(workOrderDTO.getEstimatedDurationMinutes());
    workOrder.setPriority(workOrderDTO.getPriority());
    workOrder.setScheduledEnd(workOrderDTO.getScheduledEnd());
    workOrder.setScheduledStart(workOrderDTO.getScheduledStart());
    workOrder.setStatus(workOrderDTO.getStatus());
    workOrder.setTitle(workOrderDTO.getTitle());
    workOrder.setVersion(workOrderDTO.getVersion());
    workOrder.setWorkOrderNo(workOrderDTO.getWorkOrderNo());
    workOrder.setAssetId(workOrderDTO.getAssetId());
    workOrder.setLastModifiedById(workOrderDTO.getLastModifiedById());
    workOrder.setLocationId(workOrderDTO.getLocationId());
    final Organization organization =
        workOrderDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(workOrderDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    workOrder.setOrganization(organization);
    return workOrder;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrder organizationWorkOrder =
        workOrderRepository.findFirstByOrganizationId(event.getId());
    if (organizationWorkOrder != null) {
      referencedException.setKey("organization.workOrder.organization.referenced");
      referencedException.addParam(organizationWorkOrder.getId());
      throw referencedException;
    }
  }
}
