package com.fieldops.fieldops_api.device_sync_state.service;

import com.fieldops.fieldops_api.device_sync_state.domain.DeviceSyncState;
import com.fieldops.fieldops_api.device_sync_state.model.DeviceSyncStateDTO;
import com.fieldops.fieldops_api.device_sync_state.repos.DeviceSyncStateRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class DeviceSyncStateService {

    private final DeviceSyncStateRepository deviceSyncStateRepository;
    private final UserRepository userRepository;

    public DeviceSyncStateService(final DeviceSyncStateRepository deviceSyncStateRepository,
            final UserRepository userRepository) {
        this.deviceSyncStateRepository = deviceSyncStateRepository;
        this.userRepository = userRepository;
    }

    public List<DeviceSyncStateDTO> findAll() {
        final List<DeviceSyncState> deviceSyncStates = deviceSyncStateRepository.findAll(Sort.by("id"));
        return deviceSyncStates.stream()
                .map(deviceSyncState -> mapToDTO(deviceSyncState, new DeviceSyncStateDTO()))
                .toList();
    }

    public DeviceSyncStateDTO get(final UUID id) {
        return deviceSyncStateRepository.findById(id)
                .map(deviceSyncState -> mapToDTO(deviceSyncState, new DeviceSyncStateDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final DeviceSyncStateDTO deviceSyncStateDTO) {
        final DeviceSyncState deviceSyncState = new DeviceSyncState();
        mapToEntity(deviceSyncStateDTO, deviceSyncState);
        return deviceSyncStateRepository.save(deviceSyncState).getId();
    }

    public void update(final UUID id, final DeviceSyncStateDTO deviceSyncStateDTO) {
        final DeviceSyncState deviceSyncState = deviceSyncStateRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(deviceSyncStateDTO, deviceSyncState);
        deviceSyncStateRepository.save(deviceSyncState);
    }

    public void delete(final UUID id) {
        final DeviceSyncState deviceSyncState = deviceSyncStateRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        deviceSyncStateRepository.delete(deviceSyncState);
    }

    private DeviceSyncStateDTO mapToDTO(final DeviceSyncState deviceSyncState,
            final DeviceSyncStateDTO deviceSyncStateDTO) {
        deviceSyncStateDTO.setId(deviceSyncState.getId());
        deviceSyncStateDTO.setDeviceId(deviceSyncState.getDeviceId());
        deviceSyncStateDTO.setTableName(deviceSyncState.getTableName());
        deviceSyncStateDTO.setLastChangeVersion(deviceSyncState.getLastChangeVersion());
        deviceSyncStateDTO.setLastSyncAt(deviceSyncState.getLastSyncAt());
        deviceSyncStateDTO.setCreatedAt(deviceSyncState.getCreatedAt());
        deviceSyncStateDTO.setUpdatedAt(deviceSyncState.getUpdatedAt());
        deviceSyncStateDTO.setUser(deviceSyncState.getUser() == null ? null : deviceSyncState.getUser().getId());
        return deviceSyncStateDTO;
    }

    private DeviceSyncState mapToEntity(final DeviceSyncStateDTO deviceSyncStateDTO,
            final DeviceSyncState deviceSyncState) {
        deviceSyncState.setDeviceId(deviceSyncStateDTO.getDeviceId());
        deviceSyncState.setTableName(deviceSyncStateDTO.getTableName());
        deviceSyncState.setLastChangeVersion(deviceSyncStateDTO.getLastChangeVersion());
        deviceSyncState.setLastSyncAt(deviceSyncStateDTO.getLastSyncAt());
        deviceSyncState.setCreatedAt(deviceSyncStateDTO.getCreatedAt());
        deviceSyncState.setUpdatedAt(deviceSyncStateDTO.getUpdatedAt());
        final User user = deviceSyncStateDTO.getUser() == null ? null : userRepository.findById(deviceSyncStateDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        deviceSyncState.setUser(user);
        return deviceSyncState;
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final DeviceSyncState userDeviceSyncState = deviceSyncStateRepository.findFirstByUserId(event.getId());
        if (userDeviceSyncState != null) {
            referencedException.setKey("user.deviceSyncState.user.referenced");
            referencedException.addParam(userDeviceSyncState.getId());
            throw referencedException;
        }
    }

}
