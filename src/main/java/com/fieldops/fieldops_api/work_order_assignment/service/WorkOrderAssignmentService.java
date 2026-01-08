package com.fieldops.fieldops_api.work_order_assignment.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrder;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
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
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    public WorkOrderAssignmentService(
            final WorkOrderAssignmentRepository workOrderAssignmentRepository,
            final WorkOrderRepository workOrderRepository, final UserRepository userRepository) {
        this.workOrderAssignmentRepository = workOrderAssignmentRepository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
    }

    public List<WorkOrderAssignmentDTO> findAll() {
        final List<WorkOrderAssignment> workOrderAssignments = workOrderAssignmentRepository.findAll(Sort.by("id"));
        return workOrderAssignments.stream()
                .map(workOrderAssignment -> mapToDTO(workOrderAssignment, new WorkOrderAssignmentDTO()))
                .toList();
    }

    public WorkOrderAssignmentDTO get(final UUID id) {
        return workOrderAssignmentRepository.findById(id)
                .map(workOrderAssignment -> mapToDTO(workOrderAssignment, new WorkOrderAssignmentDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
        final WorkOrderAssignment workOrderAssignment = new WorkOrderAssignment();
        mapToEntity(workOrderAssignmentDTO, workOrderAssignment);
        return workOrderAssignmentRepository.save(workOrderAssignment).getId();
    }

    public void update(final UUID id, final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
        final WorkOrderAssignment workOrderAssignment = workOrderAssignmentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(workOrderAssignmentDTO, workOrderAssignment);
        workOrderAssignmentRepository.save(workOrderAssignment);
    }

    public void delete(final UUID id) {
        final WorkOrderAssignment workOrderAssignment = workOrderAssignmentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        workOrderAssignmentRepository.delete(workOrderAssignment);
    }

    private WorkOrderAssignmentDTO mapToDTO(final WorkOrderAssignment workOrderAssignment,
            final WorkOrderAssignmentDTO workOrderAssignmentDTO) {
        workOrderAssignmentDTO.setId(workOrderAssignment.getId());
        workOrderAssignmentDTO.setAssignedAt(workOrderAssignment.getAssignedAt());
        workOrderAssignmentDTO.setUnassignedAt(workOrderAssignment.getUnassignedAt());
        workOrderAssignmentDTO.setActive(workOrderAssignment.getActive());
        workOrderAssignmentDTO.setChangeVersion(workOrderAssignment.getChangeVersion());
        workOrderAssignmentDTO.setWorkOrder(workOrderAssignment.getWorkOrder() == null ? null : workOrderAssignment.getWorkOrder().getId());
        workOrderAssignmentDTO.setEngineerUser(workOrderAssignment.getEngineerUser() == null ? null : workOrderAssignment.getEngineerUser().getId());
        return workOrderAssignmentDTO;
    }

    private WorkOrderAssignment mapToEntity(final WorkOrderAssignmentDTO workOrderAssignmentDTO,
            final WorkOrderAssignment workOrderAssignment) {
        workOrderAssignment.setAssignedAt(workOrderAssignmentDTO.getAssignedAt());
        workOrderAssignment.setUnassignedAt(workOrderAssignmentDTO.getUnassignedAt());
        workOrderAssignment.setActive(workOrderAssignmentDTO.getActive());
        workOrderAssignment.setChangeVersion(workOrderAssignmentDTO.getChangeVersion());
        final WorkOrder workOrder = workOrderAssignmentDTO.getWorkOrder() == null ? null : workOrderRepository.findById(workOrderAssignmentDTO.getWorkOrder())
                .orElseThrow(() -> new NotFoundException("workOrder not found"));
        workOrderAssignment.setWorkOrder(workOrder);
        final User engineerUser = workOrderAssignmentDTO.getEngineerUser() == null ? null : userRepository.findById(workOrderAssignmentDTO.getEngineerUser())
                .orElseThrow(() -> new NotFoundException("engineerUser not found"));
        workOrderAssignment.setEngineerUser(engineerUser);
        return workOrderAssignment;
    }

    @EventListener(BeforeDeleteWorkOrder.class)
    public void on(final BeforeDeleteWorkOrder event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderAssignment workOrderWorkOrderAssignment = workOrderAssignmentRepository.findFirstByWorkOrderId(event.getId());
        if (workOrderWorkOrderAssignment != null) {
            referencedException.setKey("workOrder.workOrderAssignment.workOrder.referenced");
            referencedException.addParam(workOrderWorkOrderAssignment.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderAssignment engineerUserWorkOrderAssignment = workOrderAssignmentRepository.findFirstByEngineerUserId(event.getId());
        if (engineerUserWorkOrderAssignment != null) {
            referencedException.setKey("user.workOrderAssignment.engineerUser.referenced");
            referencedException.addParam(engineerUserWorkOrderAssignment.getId());
            throw referencedException;
        }
    }

}
