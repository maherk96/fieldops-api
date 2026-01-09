package com.fieldops.fieldops_api.work_order_assignment.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order_assignment.domain.WorkOrderAssignment;
import com.fieldops.fieldops_api.work_order_assignment.model.WorkOrderAssignmentDTO;
import com.fieldops.fieldops_api.work_order_assignment.repos.WorkOrderAssignmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderAssignmentService {

  private final WorkOrderAssignmentRepository workOrderAssignmentRepository;
  private final OrganizationRepository organizationRepository;

  public WorkOrderAssignmentService(
      final WorkOrderAssignmentRepository workOrderAssignmentRepository,
      final OrganizationRepository organizationRepository) {
    this.workOrderAssignmentRepository = workOrderAssignmentRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<WorkOrderAssignmentDTO> findAll() {
    final List<WorkOrderAssignment> workOrderAssignments =
        workOrderAssignmentRepository.findAll(Sort.by("id"));
    return workOrderAssignments.stream()
        .map(workOrderAssignment -> mapToDTO(workOrderAssignment, new WorkOrderAssignmentDTO()))
        .toList();
  }

  public WorkOrderAssignmentDTO get(final UUID id) {
    return workOrderAssignmentRepository
        .findById(id)
        .map(workOrderAssignment -> mapToDTO(workOrderAssignment, new WorkOrderAssignmentDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
    final WorkOrderAssignment workOrderAssignment = new WorkOrderAssignment();
    mapToEntity(workOrderAssignmentDTO, workOrderAssignment);
    return workOrderAssignmentRepository.save(workOrderAssignment).getId();
  }

  public void update(final UUID id, final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
    final WorkOrderAssignment workOrderAssignment =
        workOrderAssignmentRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(workOrderAssignmentDTO, workOrderAssignment);
    workOrderAssignmentRepository.save(workOrderAssignment);
  }

  public void delete(final UUID id) {
    final WorkOrderAssignment workOrderAssignment =
        workOrderAssignmentRepository.findById(id).orElseThrow(NotFoundException::new);
    workOrderAssignmentRepository.delete(workOrderAssignment);
  }

  private WorkOrderAssignmentDTO mapToDTO(
      final WorkOrderAssignment workOrderAssignment,
      final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
    workOrderAssignmentDTO.setId(workOrderAssignment.getId());
    workOrderAssignmentDTO.setActive(workOrderAssignment.getActive());
    workOrderAssignmentDTO.setAssignedAt(workOrderAssignment.getAssignedAt());
    workOrderAssignmentDTO.setChangeVersion(workOrderAssignment.getChangeVersion());
    workOrderAssignmentDTO.setDateCreated(workOrderAssignment.getDateCreated());
    workOrderAssignmentDTO.setLastUpdated(workOrderAssignment.getLastUpdated());
    workOrderAssignmentDTO.setUnassignedAt(workOrderAssignment.getUnassignedAt());
    workOrderAssignmentDTO.setEngineerUserId(workOrderAssignment.getEngineerUserId());
    workOrderAssignmentDTO.setWorkOrderId(workOrderAssignment.getWorkOrderId());
    workOrderAssignmentDTO.setOrganization(
        workOrderAssignment.getOrganization() == null
            ? null
            : workOrderAssignment.getOrganization().getId());
    return workOrderAssignmentDTO;
  }

  private WorkOrderAssignment mapToEntity(
      final WorkOrderAssignmentDTO workOrderAssignmentDTO,
      final WorkOrderAssignment workOrderAssignment) {
    workOrderAssignment.setActive(workOrderAssignmentDTO.getActive());
    workOrderAssignment.setAssignedAt(workOrderAssignmentDTO.getAssignedAt());
    workOrderAssignment.setChangeVersion(workOrderAssignmentDTO.getChangeVersion());
    workOrderAssignment.setDateCreated(workOrderAssignmentDTO.getDateCreated());
    workOrderAssignment.setLastUpdated(workOrderAssignmentDTO.getLastUpdated());
    workOrderAssignment.setUnassignedAt(workOrderAssignmentDTO.getUnassignedAt());
    workOrderAssignment.setEngineerUserId(workOrderAssignmentDTO.getEngineerUserId());
    workOrderAssignment.setWorkOrderId(workOrderAssignmentDTO.getWorkOrderId());
    final Organization organization =
        workOrderAssignmentDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(workOrderAssignmentDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    workOrderAssignment.setOrganization(organization);
    return workOrderAssignment;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderAssignment organizationWorkOrderAssignment =
        workOrderAssignmentRepository.findFirstByOrganizationId(event.getId());
    if (organizationWorkOrderAssignment != null) {
      referencedException.setKey("organization.workOrderAssignment.organization.referenced");
      referencedException.addParam(organizationWorkOrderAssignment.getId());
      throw referencedException;
    }
  }
}
