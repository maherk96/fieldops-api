package com.fieldops.fieldops_api.work_order.service;

import com.fieldops.fieldops_api.asset.domain.Asset;
import com.fieldops.fieldops_api.asset.repos.AssetRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteAsset;
import com.fieldops.fieldops_api.events.BeforeDeleteLocation;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.events.BeforeDeleteWorkOrder;
import com.fieldops.fieldops_api.location.domain.Location;
import com.fieldops.fieldops_api.location.repos.LocationRepository;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import com.fieldops.fieldops_api.work_order.model.WorkOrderDTO;
import com.fieldops.fieldops_api.work_order.repos.WorkOrderRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public WorkOrderService(final WorkOrderRepository workOrderRepository,
            final LocationRepository locationRepository, final AssetRepository assetRepository,
            final UserRepository userRepository, final ApplicationEventPublisher publisher) {
        this.workOrderRepository = workOrderRepository;
        this.locationRepository = locationRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    public List<WorkOrderDTO> findAll() {
        final List<WorkOrder> workOrders = workOrderRepository.findAll(Sort.by("id"));
        return workOrders.stream()
                .map(workOrder -> mapToDTO(workOrder, new WorkOrderDTO()))
                .toList();
    }

    public WorkOrderDTO get(final UUID id) {
        return workOrderRepository.findById(id)
                .map(workOrder -> mapToDTO(workOrder, new WorkOrderDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final WorkOrderDTO workOrderDTO) {
        final WorkOrder workOrder = new WorkOrder();
        mapToEntity(workOrderDTO, workOrder);
        return workOrderRepository.save(workOrder).getId();
    }

    public void update(final UUID id, final WorkOrderDTO workOrderDTO) {
        final WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(workOrderDTO, workOrder);
        workOrderRepository.save(workOrder);
    }

    public void delete(final UUID id) {
        final WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteWorkOrder(id));
        workOrderRepository.delete(workOrder);
    }

    private WorkOrderDTO mapToDTO(final WorkOrder workOrder, final WorkOrderDTO workOrderDTO) {
        workOrderDTO.setId(workOrder.getId());
        workOrderDTO.setWorkOrderNo(workOrder.getWorkOrderNo());
        workOrderDTO.setTitle(workOrder.getTitle());
        workOrderDTO.setDescription(workOrder.getDescription());
        workOrderDTO.setPriority(workOrder.getPriority());
        workOrderDTO.setStatus(workOrder.getStatus());
        workOrderDTO.setScheduledStart(workOrder.getScheduledStart());
        workOrderDTO.setScheduledEnd(workOrder.getScheduledEnd());
        workOrderDTO.setEstimatedDurationMinutes(workOrder.getEstimatedDurationMinutes());
        workOrderDTO.setActualStart(workOrder.getActualStart());
        workOrderDTO.setActualEnd(workOrder.getActualEnd());
        workOrderDTO.setCompletedAt(workOrder.getCompletedAt());
        workOrderDTO.setCompletionNotes(workOrder.getCompletionNotes());
        workOrderDTO.setDeletedAt(workOrder.getDeletedAt());
        workOrderDTO.setVersion(workOrder.getVersion());
        workOrderDTO.setChangeVersion(workOrder.getChangeVersion());
        workOrderDTO.setCreatedAt(workOrder.getCreatedAt());
        workOrderDTO.setUpdatedAt(workOrder.getUpdatedAt());
        workOrderDTO.setLocation(workOrder.getLocation() == null ? null : workOrder.getLocation().getId());
        workOrderDTO.setAsset(workOrder.getAsset() == null ? null : workOrder.getAsset().getId());
        workOrderDTO.setLastModifiedBy(workOrder.getLastModifiedBy() == null ? null : workOrder.getLastModifiedBy().getId());
        return workOrderDTO;
    }

    private WorkOrder mapToEntity(final WorkOrderDTO workOrderDTO, final WorkOrder workOrder) {
        workOrder.setWorkOrderNo(workOrderDTO.getWorkOrderNo());
        workOrder.setTitle(workOrderDTO.getTitle());
        workOrder.setDescription(workOrderDTO.getDescription());
        workOrder.setPriority(workOrderDTO.getPriority());
        workOrder.setStatus(workOrderDTO.getStatus());
        workOrder.setScheduledStart(workOrderDTO.getScheduledStart());
        workOrder.setScheduledEnd(workOrderDTO.getScheduledEnd());
        workOrder.setEstimatedDurationMinutes(workOrderDTO.getEstimatedDurationMinutes());
        workOrder.setActualStart(workOrderDTO.getActualStart());
        workOrder.setActualEnd(workOrderDTO.getActualEnd());
        workOrder.setCompletedAt(workOrderDTO.getCompletedAt());
        workOrder.setCompletionNotes(workOrderDTO.getCompletionNotes());
        workOrder.setDeletedAt(workOrderDTO.getDeletedAt());
        workOrder.setVersion(workOrderDTO.getVersion());
        workOrder.setChangeVersion(workOrderDTO.getChangeVersion());
        workOrder.setCreatedAt(workOrderDTO.getCreatedAt());
        workOrder.setUpdatedAt(workOrderDTO.getUpdatedAt());
        final Location location = workOrderDTO.getLocation() == null ? null : locationRepository.findById(workOrderDTO.getLocation())
                .orElseThrow(() -> new NotFoundException("location not found"));
        workOrder.setLocation(location);
        final Asset asset = workOrderDTO.getAsset() == null ? null : assetRepository.findById(workOrderDTO.getAsset())
                .orElseThrow(() -> new NotFoundException("asset not found"));
        workOrder.setAsset(asset);
        final User lastModifiedBy = workOrderDTO.getLastModifiedBy() == null ? null : userRepository.findById(workOrderDTO.getLastModifiedBy())
                .orElseThrow(() -> new NotFoundException("lastModifiedBy not found"));
        workOrder.setLastModifiedBy(lastModifiedBy);
        return workOrder;
    }

    @EventListener(BeforeDeleteLocation.class)
    public void on(final BeforeDeleteLocation event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrder locationWorkOrder = workOrderRepository.findFirstByLocationId(event.getId());
        if (locationWorkOrder != null) {
            referencedException.setKey("location.workOrder.location.referenced");
            referencedException.addParam(locationWorkOrder.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteAsset.class)
    public void on(final BeforeDeleteAsset event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrder assetWorkOrder = workOrderRepository.findFirstByAssetId(event.getId());
        if (assetWorkOrder != null) {
            referencedException.setKey("asset.workOrder.asset.referenced");
            referencedException.addParam(assetWorkOrder.getId());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final WorkOrder lastModifiedByWorkOrder = workOrderRepository.findFirstByLastModifiedById(event.getId());
        if (lastModifiedByWorkOrder != null) {
            referencedException.setKey("user.workOrder.lastModifiedBy.referenced");
            referencedException.addParam(lastModifiedByWorkOrder.getId());
            throw referencedException;
        }
    }

}
