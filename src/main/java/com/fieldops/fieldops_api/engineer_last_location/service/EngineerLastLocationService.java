package com.fieldops.fieldops_api.engineer_last_location.service;

import com.fieldops.fieldops_api.engineer_last_location.domain.EngineerLastLocation;
import com.fieldops.fieldops_api.engineer_last_location.model.EngineerLastLocationDTO;
import com.fieldops.fieldops_api.engineer_last_location.repos.EngineerLastLocationRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class EngineerLastLocationService {

    private final EngineerLastLocationRepository engineerLastLocationRepository;
    private final UserRepository userRepository;

    public EngineerLastLocationService(
            final EngineerLastLocationRepository engineerLastLocationRepository,
            final UserRepository userRepository) {
        this.engineerLastLocationRepository = engineerLastLocationRepository;
        this.userRepository = userRepository;
    }

    public List<EngineerLastLocationDTO> findAll() {
        final List<EngineerLastLocation> engineerLastLocations = engineerLastLocationRepository.findAll(Sort.by("id"));
        return engineerLastLocations.stream()
                .map(engineerLastLocation -> mapToDTO(engineerLastLocation, new EngineerLastLocationDTO()))
                .toList();
    }

    public EngineerLastLocationDTO get(final Long id) {
        return engineerLastLocationRepository.findById(id)
                .map(engineerLastLocation -> mapToDTO(engineerLastLocation, new EngineerLastLocationDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final EngineerLastLocationDTO engineerLastLocationDTO) {
        final EngineerLastLocation engineerLastLocation = new EngineerLastLocation();
        mapToEntity(engineerLastLocationDTO, engineerLastLocation);
        return engineerLastLocationRepository.save(engineerLastLocation).getId();
    }

    public void update(final Long id, final EngineerLastLocationDTO engineerLastLocationDTO) {
        final EngineerLastLocation engineerLastLocation = engineerLastLocationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(engineerLastLocationDTO, engineerLastLocation);
        engineerLastLocationRepository.save(engineerLastLocation);
    }

    public void delete(final Long id) {
        final EngineerLastLocation engineerLastLocation = engineerLastLocationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        engineerLastLocationRepository.delete(engineerLastLocation);
    }

    private EngineerLastLocationDTO mapToDTO(final EngineerLastLocation engineerLastLocation,
            final EngineerLastLocationDTO engineerLastLocationDTO) {
        engineerLastLocationDTO.setId(engineerLastLocation.getId());
        engineerLastLocationDTO.setLat(engineerLastLocation.getLat());
        engineerLastLocationDTO.setLng(engineerLastLocation.getLng());
        engineerLastLocationDTO.setAccuracyMeters(engineerLastLocation.getAccuracyMeters());
        engineerLastLocationDTO.setRecordedAt(engineerLastLocation.getRecordedAt());
        engineerLastLocationDTO.setUpdatedAt(engineerLastLocation.getUpdatedAt());
        engineerLastLocationDTO.setEngineerUser(engineerLastLocation.getEngineerUser() == null ? null : engineerLastLocation.getEngineerUser().getId());
        return engineerLastLocationDTO;
    }

    private EngineerLastLocation mapToEntity(final EngineerLastLocationDTO engineerLastLocationDTO,
            final EngineerLastLocation engineerLastLocation) {
        engineerLastLocation.setLat(engineerLastLocationDTO.getLat());
        engineerLastLocation.setLng(engineerLastLocationDTO.getLng());
        engineerLastLocation.setAccuracyMeters(engineerLastLocationDTO.getAccuracyMeters());
        engineerLastLocation.setRecordedAt(engineerLastLocationDTO.getRecordedAt());
        engineerLastLocation.setUpdatedAt(engineerLastLocationDTO.getUpdatedAt());
        final User engineerUser = engineerLastLocationDTO.getEngineerUser() == null ? null : userRepository.findById(engineerLastLocationDTO.getEngineerUser())
                .orElseThrow(() -> new NotFoundException("engineerUser not found"));
        engineerLastLocation.setEngineerUser(engineerUser);
        return engineerLastLocation;
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final EngineerLastLocation engineerUserEngineerLastLocation = engineerLastLocationRepository.findFirstByEngineerUserId(event.getId());
        if (engineerUserEngineerLastLocation != null) {
            referencedException.setKey("user.engineerLastLocation.engineerUser.referenced");
            referencedException.addParam(engineerUserEngineerLastLocation.getId());
            throw referencedException;
        }
    }

}
