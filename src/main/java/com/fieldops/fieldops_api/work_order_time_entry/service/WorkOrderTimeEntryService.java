package com.fieldops.fieldops_api.work_order_time_entry.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order_time_entry.domain.WorkOrderTimeEntry;
import com.fieldops.fieldops_api.work_order_time_entry.model.WorkOrderTimeEntryDTO;
import com.fieldops.fieldops_api.work_order_time_entry.repos.WorkOrderTimeEntryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderTimeEntryService {

  private final WorkOrderTimeEntryRepository workOrderTimeEntryRepository;
  private final OrganizationRepository organizationRepository;

  public WorkOrderTimeEntryService(
      final WorkOrderTimeEntryRepository workOrderTimeEntryRepository,
      final OrganizationRepository organizationRepository) {
    this.workOrderTimeEntryRepository = workOrderTimeEntryRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<WorkOrderTimeEntryDTO> findAll() {
    final List<WorkOrderTimeEntry> workOrderTimeEntries =
        workOrderTimeEntryRepository.findAll(Sort.by("id"));
    return workOrderTimeEntries.stream()
        .map(workOrderTimeEntry -> mapToDTO(workOrderTimeEntry, new WorkOrderTimeEntryDTO()))
        .toList();
  }

  public WorkOrderTimeEntryDTO get(final UUID id) {
    return workOrderTimeEntryRepository
        .findById(id)
        .map(workOrderTimeEntry -> mapToDTO(workOrderTimeEntry, new WorkOrderTimeEntryDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final WorkOrderTimeEntryDTO workOrderTimeEntryDTO) {
    final WorkOrderTimeEntry workOrderTimeEntry = new WorkOrderTimeEntry();
    mapToEntity(workOrderTimeEntryDTO, workOrderTimeEntry);
    return workOrderTimeEntryRepository.save(workOrderTimeEntry).getId();
  }

  public void update(final UUID id, final WorkOrderTimeEntryDTO workOrderTimeEntryDTO) {
    final WorkOrderTimeEntry workOrderTimeEntry =
        workOrderTimeEntryRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(workOrderTimeEntryDTO, workOrderTimeEntry);
    workOrderTimeEntryRepository.save(workOrderTimeEntry);
  }

  public void delete(final UUID id) {
    final WorkOrderTimeEntry workOrderTimeEntry =
        workOrderTimeEntryRepository.findById(id).orElseThrow(NotFoundException::new);
    workOrderTimeEntryRepository.delete(workOrderTimeEntry);
  }

  private WorkOrderTimeEntryDTO mapToDTO(
      final WorkOrderTimeEntry workOrderTimeEntry,
      final WorkOrderTimeEntryDTO workOrderTimeEntryDTO) {
    workOrderTimeEntryDTO.setId(workOrderTimeEntry.getId());
    workOrderTimeEntryDTO.setChangeVersion(workOrderTimeEntry.getChangeVersion());
    workOrderTimeEntryDTO.setDateCreated(workOrderTimeEntry.getDateCreated());
    workOrderTimeEntryDTO.setDurationMinutes(workOrderTimeEntry.getDurationMinutes());
    workOrderTimeEntryDTO.setEndTime(workOrderTimeEntry.getEndTime());
    workOrderTimeEntryDTO.setEntryType(workOrderTimeEntry.getEntryType());
    workOrderTimeEntryDTO.setLastUpdated(workOrderTimeEntry.getLastUpdated());
    workOrderTimeEntryDTO.setNotes(workOrderTimeEntry.getNotes());
    workOrderTimeEntryDTO.setStartTime(workOrderTimeEntry.getStartTime());
    workOrderTimeEntryDTO.setUpdatedAt(workOrderTimeEntry.getUpdatedAt());
    workOrderTimeEntryDTO.setEngineerUserId(workOrderTimeEntry.getEngineerUserId());
    workOrderTimeEntryDTO.setWorkOrderId(workOrderTimeEntry.getWorkOrderId());
    workOrderTimeEntryDTO.setOrganization(
        workOrderTimeEntry.getOrganization() == null
            ? null
            : workOrderTimeEntry.getOrganization().getId());
    return workOrderTimeEntryDTO;
  }

  private WorkOrderTimeEntry mapToEntity(
      final WorkOrderTimeEntryDTO workOrderTimeEntryDTO,
      final WorkOrderTimeEntry workOrderTimeEntry) {
    workOrderTimeEntry.setChangeVersion(workOrderTimeEntryDTO.getChangeVersion());
    workOrderTimeEntry.setDurationMinutes(workOrderTimeEntryDTO.getDurationMinutes());
    workOrderTimeEntry.setEndTime(workOrderTimeEntryDTO.getEndTime());
    workOrderTimeEntry.setEntryType(workOrderTimeEntryDTO.getEntryType());
    workOrderTimeEntry.setNotes(workOrderTimeEntryDTO.getNotes());
    workOrderTimeEntry.setStartTime(workOrderTimeEntryDTO.getStartTime());
    workOrderTimeEntry.setUpdatedAt(workOrderTimeEntryDTO.getUpdatedAt());
    workOrderTimeEntry.setEngineerUserId(workOrderTimeEntryDTO.getEngineerUserId());
    workOrderTimeEntry.setWorkOrderId(workOrderTimeEntryDTO.getWorkOrderId());
    final Organization organization =
        workOrderTimeEntryDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(workOrderTimeEntryDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    workOrderTimeEntry.setOrganization(organization);
    return workOrderTimeEntry;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderTimeEntry organizationWorkOrderTimeEntry =
        workOrderTimeEntryRepository.findFirstByOrganizationId(event.getId());
    if (organizationWorkOrderTimeEntry != null) {
      referencedException.setKey("organization.workOrderTimeEntry.organization.referenced");
      referencedException.addParam(organizationWorkOrderTimeEntry.getId());
      throw referencedException;
    }
  }
}
