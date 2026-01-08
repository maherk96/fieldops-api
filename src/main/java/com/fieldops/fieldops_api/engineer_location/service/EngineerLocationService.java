package com.fieldops.fieldops_api.engineer_location.service;

import com.fieldops.fieldops_api.engineer_location.domain.EngineerLocation;
import com.fieldops.fieldops_api.engineer_location.model.EngineerLocationDTO;
import com.fieldops.fieldops_api.engineer_location.repos.EngineerLocationRepository;
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
public class EngineerLocationService {

    private final EngineerLocationRepository engineerLocationRepository;
    private final UserRepository userRepository;

    public EngineerLocationService(final EngineerLocationRepository engineerLocationRepository,
            final UserRepository userRepository) {
        this.engineerLocationRepository = engineerLocationRepository;
        this.userRepository = userRepository;
    }

    public List<EngineerLocationDTO> findAll() {
        final List<EngineerLocation> engineerLocations = engineerLocationRepository.findAll(Sort.by("id"));
        return engineerLocations.stream()
                .map(engineerLocation -> mapToDTO(engineerLocation, new EngineerLocationDTO()))
                .toList();
    }

    public EngineerLocationDTO get(final UUID id) {
        return engineerLocationRepository.findById(id)
                .map(engineerLocation -> mapToDTO(engineerLocation, new EngineerLocationDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final EngineerLocationDTO engineerLocationDTO) {
        final EngineerLocation engineerLocation = new EngineerLocation();
        mapToEntity(engineerLocationDTO, engineerLocation);
        return engineerLocationRepository.save(engineerLocation).getId();
    }

    public void update(final UUID id, final EngineerLocationDTO engineerLocationDTO) {
        final EngineerLocation engineerLocation = engineerLocationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(engineerLocationDTO, engineerLocation);
        engineerLocationRepository.save(engineerLocation);
    }

    public void delete(final UUID id) {
        final EngineerLocation engineerLocation = engineerLocationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        engineerLocationRepository.delete(engineerLocation);
    }

    private EngineerLocationDTO mapToDTO(final EngineerLocation engineerLocation,
            final EngineerLocationDTO engineerLocationDTO) {
        engineerLocationDTO.setId(engineerLocation.getId());
        engineerLocationDTO.setLat(engineerLocation.getLat());
        engineerLocationDTO.setLng(engineerLocation.getLng());
        engineerLocationDTO.setAccuracyMeters(engineerLocation.getAccuracyMeters());
        engineerLocationDTO.setRecordedAt(engineerLocation.getRecordedAt());
        engineerLocationDTO.setCreatedAt(engineerLocation.getCreatedAt());
        engineerLocationDTO.setEngineerUser(engineerLocation.getEngineerUser() == null ? null : engineerLocation.getEngineerUser().getId());
        return engineerLocationDTO;
    }

    private EngineerLocation mapToEntity(final EngineerLocationDTO engineerLocationDTO,
            final EngineerLocation engineerLocation) {
        engineerLocation.setLat(engineerLocationDTO.getLat());
        engineerLocation.setLng(engineerLocationDTO.getLng());
        engineerLocation.setAccuracyMeters(engineerLocationDTO.getAccuracyMeters());
        engineerLocation.setRecordedAt(engineerLocationDTO.getRecordedAt());
        engineerLocation.setCreatedAt(engineerLocationDTO.getCreatedAt());
        final User engineerUser = engineerLocationDTO.getEngineerUser() == null ? null : userRepository.findById(engineerLocationDTO.getEngineerUser())
                .orElseThrow(() -> new NotFoundException("engineerUser not found"));
        engineerLocation.setEngineerUser(engineerUser);
        return engineerLocation;
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final EngineerLocation engineerUserEngineerLocation = engineerLocationRepository.findFirstByEngineerUserId(event.getId());
        if (engineerUserEngineerLocation != null) {
            referencedException.setKey("user.engineerLocation.engineerUser.referenced");
            referencedException.addParam(engineerUserEngineerLocation.getId());
            throw referencedException;
        }
    }

}
