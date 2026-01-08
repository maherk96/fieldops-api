package com.fieldops.fieldops_api.work_order_event.service;

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
import com.fieldops.fieldops_api.work_order_event.model.WorkOrderEventDTO;
import com.fieldops.fieldops_api.work_order_event.repos.WorkOrderEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class WorkOrderEventService {

    private final WorkOrderEventRepository workOrderEventRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public WorkOrderEventService(final WorkOrderEventRepository workOrderEventRepository,
            final WorkOrderRepository workOrderRepository, final UserRepository userRepository,
            final ApplicationEventPublisher publisher) {
        this.workOrderEventRepository = workOrderEventRepository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    public List<WorkOrderEventDTO> findAll() {
        final List<WorkOrderEvent> workOrderEvents = workOrderEventRepository.findAll(Sort.by("id"));
        return workOrderEvents.stream()
                .map(workOrderEvent -> mapToDTO(workOrderEvent, new WorkOrderEventDTO()))
                .toList();
    }

    public WorkOrderEventDTO get(final UUID id) {
        return workOrderEventRepository.findById(id)
                .map(workOrderEvent -> mapToDTO(workOrderEvent, new WorkOrderEventDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final WorkOrderEventDTO workOrderEventDTO) {
        final WorkOrderEvent workOrderEvent = new WorkOrderEvent();
        mapToEntity(workOrderEventDTO, workOrderEvent);
        return workOrderEventRepository.save(workOrderEvent).getId();
    }

    public void update(final UUID id, final WorkOrderEventDTO workOrderEventDTO) {
        final WorkOrderEvent workOrderEvent = workOrderEventRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(workOrderEventDTO, workOrderEvent);
        workOrderEventRepository.save(workOrderEvent);
    }

    public void delete(final UUID id) {
        final WorkOrderEvent workOrderEvent = workOrderEventRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteWorkOrderEvent(id));
        workOrderEventRepository.delete(workOrderEvent);
    }

    private WorkOrderEventDTO mapToDTO(final WorkOrderEvent workOrderEvent,
            final WorkOrderEventDTO workOrderEventDTO) {
        workOrderEventDTO.setId(workOrderEvent.getId());
        workOrderEventDTO.setEventType(workOrderEvent.getEventType());
        workOrderEventDTO.setClientEventId(workOrderEvent.getClientEventId());
        workOrderEventDTO.setDeviceId(workOrderEvent.getDeviceId());
        workOrderEventDTO.setPayload(workOrderEvent.getPayload());
        workOrderEventDTO.setSyncedAt(workOrderEvent.getSyncedAt());
        workOrderEventDTO.setCreatedAt(workOrderEvent.getCreatedAt());
        workOrderEventDTO.setWorkOrder(workOrderEvent.getWorkOrder() == null ? null : workOrderEvent.getWorkOrder().getId());
        workOrderEventDTO.setCreatedByUser(workOrderEvent.getCreatedByUser() == null ? null : workOrderEvent.getCreatedByUser().getId());
        return workOrderEventDTO;
    }

    private WorkOrderEvent mapToEntity(final WorkOrderEventDTO workOrderEventDTO,
            final WorkOrderEvent workOrderEvent) {
        workOrderEvent.setEventType(workOrderEventDTO.getEventType());
        workOrderEvent.setClientEventId(workOrderEventDTO.getClientEventId());
        workOrderEvent.setDeviceId(workOrderEventDTO.getDeviceId());
        workOrderEvent.setPayload(workOrderEventDTO.getPayload());
        workOrderEvent.setSyncedAt(workOrderEventDTO.getSyncedAt());
        workOrderEvent.setCreatedAt(workOrderEventDTO.getCreatedAt());
        final WorkOrder workOrder = workOrderEventDTO.getWorkOrder() == null ? null : workOrderRepository.findById(workOrderEventDTO.getWorkOrder())
                .orElseThrow(() -> new NotFoundException("workOrder not found"));
        workOrderEvent.setWorkOrder(workOrder);
        final User createdByUser = workOrderEventDTO.getCreatedByUser() == null ? null : userRepository.findById(workOrderEventDTO.getCreatedByUser())
                .orElseThrow(() -> new NotFoundException("createdByUser not found"));
        workOrderEvent.setCreatedByUser(createdByUser);
        return workOrderEvent;
    }

    @EventListener(BeforeDeleteWorkOrder.class)
    public void on(final BeforeDeleteWorkOrder event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderEvent workOrderWorkOrderEvent = workOrderEventRepository.findFirstByWorkOrderId(event.getId());
        if (workOrderWorkOrderEvent != null) {
            referencedException.setKey("workOrder.workOrderEvent.workOrder.referenced");
            referencedException.addParam(workOrderWorkOrderEvent.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrderEvent createdByUserWorkOrderEvent = workOrderEventRepository.findFirstByCreatedByUserId(event.getId());
        if (createdByUserWorkOrderEvent != null) {
            referencedException.setKey("user.workOrderEvent.createdByUser.referenced");
            referencedException.addParam(createdByUserWorkOrderEvent.getId());
            throw referencedException;
        }
    }

}
