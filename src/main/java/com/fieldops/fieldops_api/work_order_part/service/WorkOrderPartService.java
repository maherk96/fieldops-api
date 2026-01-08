package com.fieldops.fieldops_api.work_order_part.service;

import com.fieldops.fieldops_api.events.BeforeDeletePartsCatalog;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrder;
import com.fieldops.fieldops_api.parts_catalog.domain.PartsCatalog;
import com.fieldops.fieldops_api.parts_catalog.repos.PartsCatalogRepository;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
import com.fieldops.fieldops_api.work_order_part.domain.WorkOrderPart;
import com.fieldops.fieldops_api.work_order_part.model.WorkOrderPartDTO;
import com.fieldops.fieldops_api.work_order_part.repos.WorkOrderPartRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class WorkOrderPartService {

    private final WorkOrderPartRepository workOrderPartRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PartsCatalogRepository partsCatalogRepository;
    private final UserRepository userRepository;

    public WorkOrderPartService(final WorkOrderPartRepository workOrderPartRepository,
            final WorkOrderRepository workOrderRepository,
            final PartsCatalogRepository partsCatalogRepository,
            final UserRepository userRepository) {
        this.workOrderPartRepository = workOrderPartRepository;
        this.workOrderRepository = workOrderRepository;
        this.partsCatalogRepository = partsCatalogRepository;
        this.userRepository = userRepository;
    }

    public List<WorkOrderPartDTO> findAll() {
        final List<WorkOrderPart> workOrderParts = workOrderPartRepository.findAll(Sort.by("id"));
        return workOrderParts.stream()
                .map(workOrderPart -> mapToDTO(workOrderPart, new WorkOrderPartDTO()))
                .toList();
    }

    public WorkOrderPartDTO get(final UUID id) {
        return workOrderPartRepository.findById(id)
                .map(workOrderPart -> mapToDTO(workOrderPart, new WorkOrderPartDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final WorkOrderPartDTO workOrderPartDTO) {
        final WorkOrderPart workOrderPart = new WorkOrderPart();
        mapToEntity(workOrderPartDTO, workOrderPart);
        return workOrderPartRepository.save(workOrderPart).getId();
    }

    public void update(final UUID id, final WorkOrderPartDTO workOrderPartDTO) {
        final WorkOrderPart workOrderPart = workOrderPartRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(workOrderPartDTO, workOrderPart);
        workOrderPartRepository.save(workOrderPart);
    }

    public void delete(final UUID id) {
        final WorkOrderPart workOrderPart = workOrderPartRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        workOrderPartRepository.delete(workOrderPart);
    }

    private WorkOrderPartDTO mapToDTO(final WorkOrderPart workOrderPart,
            final WorkOrderPartDTO workOrderPartDTO) {
        workOrderPartDTO.setId(workOrderPart.getId());
        workOrderPartDTO.setPartNumber(workOrderPart.getPartNumber());
        workOrderPartDTO.setDescription(workOrderPart.getDescription());
        workOrderPartDTO.setQuantity(workOrderPart.getQuantity());
        workOrderPartDTO.setUnitPrice(workOrderPart.getUnitPrice());
        workOrderPartDTO.setChangeVersion(workOrderPart.getChangeVersion());
        workOrderPartDTO.setCreatedAt(workOrderPart.getCreatedAt());
        workOrderPartDTO.setWorkOrder(workOrderPart.getWorkOrder() == null ? null : workOrderPart.getWorkOrder().getId());
        workOrderPartDTO.setPart(workOrderPart.getPart() == null ? null : workOrderPart.getPart().getId());
        workOrderPartDTO.setRecordedByUser(workOrderPart.getRecordedByUser() == null ? null : workOrderPart.getRecordedByUser().getId());
        return workOrderPartDTO;
    }

    private WorkOrderPart mapToEntity(final WorkOrderPartDTO workOrderPartDTO,
            final WorkOrderPart workOrderPart) {
        workOrderPart.setPartNumber(workOrderPartDTO.getPartNumber());
        workOrderPart.setDescription(workOrderPartDTO.getDescription());
        workOrderPart.setQuantity(workOrderPartDTO.getQuantity());
        workOrderPart.setUnitPrice(workOrderPartDTO.getUnitPrice());
        workOrderPart.setChangeVersion(workOrderPartDTO.getChangeVersion());
        workOrderPart.setCreatedAt(workOrderPartDTO.getCreatedAt());
        final WorkOrder workOrder = workOrderPartDTO.getWorkOrder() == null ? null : workOrderRepository.findById(workOrderPartDTO.getWorkOrder())
                .orElseThrow(() -> new NotFoundException("workOrder not found"));
        workOrderPart.setWorkOrder(workOrder);
        final PartsCatalog part = workOrderPartDTO.getPart() == null ? null : partsCatalogRepository.findById(workOrderPartDTO.getPart())
                .orElseThrow(() -> new NotFoundException("part not found"));
        workOrderPart.setPart(part);
        final User recordedByUser = workOrderPartDTO.getRecordedByUser() == null ? null : userRepository.findById(workOrderPartDTO.getRecordedByUser())
                .orElseThrow(() -> new NotFoundException("recordedByUser not found"));
        workOrderPart.setRecordedByUser(recordedByUser);
        return workOrderPart;
    }

    @EventListener(BeforeDeleteWorkOrder.class)
    public void on(final BeforeDeleteWorkOrder event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderPart workOrderWorkOrderPart = workOrderPartRepository.findFirstByWorkOrderId(event.getId());
        if (workOrderWorkOrderPart != null) {
            referencedException.setKey("workOrder.workOrderPart.workOrder.referenced");
            referencedException.addParam(workOrderWorkOrderPart.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeletePartsCatalog.class)
    public void on(final BeforeDeletePartsCatalog event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderPart partWorkOrderPart = workOrderPartRepository.findFirstByPartId(event.getId());
        if (partWorkOrderPart != null) {
            referencedException.setKey("partsCatalog.workOrderPart.part.referenced");
            referencedException.addParam(partWorkOrderPart.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderPart recordedByUserWorkOrderPart = workOrderPartRepository.findFirstByRecordedByUserId(event.getId());
        if (recordedByUserWorkOrderPart != null) {
            referencedException.setKey("user.workOrderPart.recordedByUser.referenced");
            referencedException.addParam(recordedByUserWorkOrderPart.getId());
            throw referencedException;
        }
    }

}
