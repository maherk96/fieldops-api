package com.fieldops.fieldops_api.work_order_time_entry.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrder;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
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
  private final WorkOrderRepository workOrderRepository;
  private final UserRepository userRepository;

  public WorkOrderTimeEntryService(
      final WorkOrderTimeEntryRepository workOrderTimeEntryRepository,
      final WorkOrderRepository workOrderRepository,
      final UserRepository userRepository) {
    this.workOrderTimeEntryRepository = workOrderTimeEntryRepository;
    this.workOrderRepository = workOrderRepository;
    this.userRepository = userRepository;
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
    workOrderTimeEntryDTO.setEntryType(workOrderTimeEntry.getEntryType());
    workOrderTimeEntryDTO.setStartTime(workOrderTimeEntry.getStartTime());
    workOrderTimeEntryDTO.setEndTime(workOrderTimeEntry.getEndTime());
    workOrderTimeEntryDTO.setDurationMinutes(workOrderTimeEntry.getDurationMinutes());
    workOrderTimeEntryDTO.setNotes(workOrderTimeEntry.getNotes());
    workOrderTimeEntryDTO.setChangeVersion(workOrderTimeEntry.getChangeVersion());
    workOrderTimeEntryDTO.setCreatedAt(workOrderTimeEntry.getCreatedAt());
    workOrderTimeEntryDTO.setUpdatedAt(workOrderTimeEntry.getUpdatedAt());
    workOrderTimeEntryDTO.setWorkOrder(
        workOrderTimeEntry.getWorkOrder() == null
            ? null
            : workOrderTimeEntry.getWorkOrder().getId());
    workOrderTimeEntryDTO.setEngineerUser(
        workOrderTimeEntry.getEngineerUser() == null
            ? null
            : workOrderTimeEntry.getEngineerUser().getId());
    return workOrderTimeEntryDTO;
  }

  private WorkOrderTimeEntry mapToEntity(
      final WorkOrderTimeEntryDTO workOrderTimeEntryDTO,
      final WorkOrderTimeEntry workOrderTimeEntry) {
    workOrderTimeEntry.setEntryType(workOrderTimeEntryDTO.getEntryType());
    workOrderTimeEntry.setStartTime(workOrderTimeEntryDTO.getStartTime());
    workOrderTimeEntry.setEndTime(workOrderTimeEntryDTO.getEndTime());
    workOrderTimeEntry.setDurationMinutes(workOrderTimeEntryDTO.getDurationMinutes());
    workOrderTimeEntry.setNotes(workOrderTimeEntryDTO.getNotes());
    workOrderTimeEntry.setChangeVersion(workOrderTimeEntryDTO.getChangeVersion());
    workOrderTimeEntry.setCreatedAt(workOrderTimeEntryDTO.getCreatedAt());
    workOrderTimeEntry.setUpdatedAt(workOrderTimeEntryDTO.getUpdatedAt());
    final WorkOrder workOrder =
        workOrderTimeEntryDTO.getWorkOrder() == null
            ? null
            : workOrderRepository
                .findById(workOrderTimeEntryDTO.getWorkOrder())
                .orElseThrow(() -> new NotFoundException("workOrder not found"));
    workOrderTimeEntry.setWorkOrder(workOrder);
    final User engineerUser =
        workOrderTimeEntryDTO.getEngineerUser() == null
            ? null
            : userRepository
                .findById(workOrderTimeEntryDTO.getEngineerUser())
                .orElseThrow(() -> new NotFoundException("engineerUser not found"));
    workOrderTimeEntry.setEngineerUser(engineerUser);
    return workOrderTimeEntry;
  }

  @EventListener(BeforeDeleteWorkOrder.class)
  public void on(final BeforeDeleteWorkOrder event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderTimeEntry workOrderWorkOrderTimeEntry =
        workOrderTimeEntryRepository.findFirstByWorkOrderId(event.getId());
    if (workOrderWorkOrderTimeEntry != null) {
      referencedException.setKey("workOrder.workOrderTimeEntry.workOrder.referenced");
      referencedException.addParam(workOrderWorkOrderTimeEntry.getId());
      throw referencedException;
    }
  }

  @EventListener(BeforeDeleteUser.class)
  public void on(final BeforeDeleteUser event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderTimeEntry engineerUserWorkOrderTimeEntry =
        workOrderTimeEntryRepository.findFirstByEngineerUserId(event.getId());
    if (engineerUserWorkOrderTimeEntry != null) {
      referencedException.setKey("user.workOrderTimeEntry.engineerUser.referenced");
      referencedException.addParam(engineerUserWorkOrderTimeEntry.getId());
      throw referencedException;
    }
  }
}
