package com.fieldops.fieldops_api.work_order_event.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import com.fieldops.fieldops_api.work_order_event.model.WorkOrderEventDTO;
import com.fieldops.fieldops_api.work_order_event.repos.WorkOrderEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderEventService {

  private final WorkOrderEventRepository workOrderEventRepository;
  private final OrganizationRepository organizationRepository;

  public WorkOrderEventService(
      final WorkOrderEventRepository workOrderEventRepository,
      final OrganizationRepository organizationRepository) {
    this.workOrderEventRepository = workOrderEventRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<WorkOrderEventDTO> findAll() {
    final List<WorkOrderEvent> workOrderEvents = workOrderEventRepository.findAll(Sort.by("id"));
    return workOrderEvents.stream()
        .map(workOrderEvent -> mapToDTO(workOrderEvent, new WorkOrderEventDTO()))
        .toList();
  }

  public WorkOrderEventDTO get(final UUID id) {
    return workOrderEventRepository
        .findById(id)
        .map(workOrderEvent -> mapToDTO(workOrderEvent, new WorkOrderEventDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final WorkOrderEventDTO workOrderEventDTO) {
    final WorkOrderEvent workOrderEvent = new WorkOrderEvent();
    mapToEntity(workOrderEventDTO, workOrderEvent);
    return workOrderEventRepository.save(workOrderEvent).getId();
  }

  public void update(final UUID id, final WorkOrderEventDTO workOrderEventDTO) {
    final WorkOrderEvent workOrderEvent =
        workOrderEventRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(workOrderEventDTO, workOrderEvent);
    workOrderEventRepository.save(workOrderEvent);
  }

  public void delete(final UUID id) {
    final WorkOrderEvent workOrderEvent =
        workOrderEventRepository.findById(id).orElseThrow(NotFoundException::new);
    workOrderEventRepository.delete(workOrderEvent);
  }

  private WorkOrderEventDTO mapToDTO(
      final WorkOrderEvent workOrderEvent, final WorkOrderEventDTO workOrderEventDTO) {
    workOrderEventDTO.setId(workOrderEvent.getId());
    workOrderEventDTO.setClientEventId(workOrderEvent.getClientEventId());
    workOrderEventDTO.setDateCreated(workOrderEvent.getDateCreated());
    workOrderEventDTO.setDeviceId(workOrderEvent.getDeviceId());
    workOrderEventDTO.setEventType(workOrderEvent.getEventType());
    workOrderEventDTO.setLastUpdated(workOrderEvent.getLastUpdated());
    workOrderEventDTO.setPayload(workOrderEvent.getPayload());
    workOrderEventDTO.setSyncedAt(workOrderEvent.getSyncedAt());
    workOrderEventDTO.setCreatedByUserId(workOrderEvent.getCreatedByUserId());
    workOrderEventDTO.setWorkOrderId(workOrderEvent.getWorkOrderId());
    workOrderEventDTO.setOrganization(
        workOrderEvent.getOrganization() == null ? null : workOrderEvent.getOrganization().getId());
    return workOrderEventDTO;
  }

  private WorkOrderEvent mapToEntity(
      final WorkOrderEventDTO workOrderEventDTO, final WorkOrderEvent workOrderEvent) {
    workOrderEvent.setClientEventId(workOrderEventDTO.getClientEventId());
    workOrderEvent.setDeviceId(workOrderEventDTO.getDeviceId());
    workOrderEvent.setEventType(workOrderEventDTO.getEventType());
    workOrderEvent.setPayload(workOrderEventDTO.getPayload());
    workOrderEvent.setSyncedAt(workOrderEventDTO.getSyncedAt());
    workOrderEvent.setCreatedByUserId(workOrderEventDTO.getCreatedByUserId());
    workOrderEvent.setWorkOrderId(workOrderEventDTO.getWorkOrderId());
    final Organization organization =
        workOrderEventDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(workOrderEventDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    workOrderEvent.setOrganization(organization);
    return workOrderEvent;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderEvent organizationWorkOrderEvent =
        workOrderEventRepository.findFirstByOrganizationId(event.getId());
    if (organizationWorkOrderEvent != null) {
      referencedException.setKey("organization.workOrderEvent.organization.referenced");
      referencedException.addParam(organizationWorkOrderEvent.getId());
      throw referencedException;
    }
  }
}
