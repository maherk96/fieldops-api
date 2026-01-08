package com.fieldops.fieldops_api.engineer_availability.service;

import com.fieldops.fieldops_api.engineer_availability.domain.EngineerAvailability;
import com.fieldops.fieldops_api.engineer_availability.model.EngineerAvailabilityDTO;
import com.fieldops.fieldops_api.engineer_availability.repos.EngineerAvailabilityRepository;
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
public class EngineerAvailabilityService {

    private final EngineerAvailabilityRepository engineerAvailabilityRepository;
    private final UserRepository userRepository;

    public EngineerAvailabilityService(
            final EngineerAvailabilityRepository engineerAvailabilityRepository,
            final UserRepository userRepository) {
        this.engineerAvailabilityRepository = engineerAvailabilityRepository;
        this.userRepository = userRepository;
    }

    public List<EngineerAvailabilityDTO> findAll() {
        final List<EngineerAvailability> engineerAvailabilities = engineerAvailabilityRepository.findAll(Sort.by("id"));
        return engineerAvailabilities.stream()
                .map(engineerAvailability -> mapToDTO(engineerAvailability, new EngineerAvailabilityDTO()))
                .toList();
    }

    public EngineerAvailabilityDTO get(final UUID id) {
        return engineerAvailabilityRepository.findById(id)
                .map(engineerAvailability -> mapToDTO(engineerAvailability, new EngineerAvailabilityDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final EngineerAvailabilityDTO engineerAvailabilityDTO) {
        final EngineerAvailability engineerAvailability = new EngineerAvailability();
        mapToEntity(engineerAvailabilityDTO, engineerAvailability);
        return engineerAvailabilityRepository.save(engineerAvailability).getId();
    }

    public void update(final UUID id, final EngineerAvailabilityDTO engineerAvailabilityDTO) {
        final EngineerAvailability engineerAvailability = engineerAvailabilityRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(engineerAvailabilityDTO, engineerAvailability);
        engineerAvailabilityRepository.save(engineerAvailability);
    }

    public void delete(final UUID id) {
        final EngineerAvailability engineerAvailability = engineerAvailabilityRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        engineerAvailabilityRepository.delete(engineerAvailability);
    }

    private EngineerAvailabilityDTO mapToDTO(final EngineerAvailability engineerAvailability,
            final EngineerAvailabilityDTO engineerAvailabilityDTO) {
        engineerAvailabilityDTO.setId(engineerAvailability.getId());
        engineerAvailabilityDTO.setAvailabilityType(engineerAvailability.getAvailabilityType());
        engineerAvailabilityDTO.setStartTime(engineerAvailability.getStartTime());
        engineerAvailabilityDTO.setEndTime(engineerAvailability.getEndTime());
        engineerAvailabilityDTO.setNotes(engineerAvailability.getNotes());
        engineerAvailabilityDTO.setCreatedAt(engineerAvailability.getCreatedAt());
        engineerAvailabilityDTO.setEngineerUser(engineerAvailability.getEngineerUser() == null ? null : engineerAvailability.getEngineerUser().getId());
        return engineerAvailabilityDTO;
    }

    private EngineerAvailability mapToEntity(final EngineerAvailabilityDTO engineerAvailabilityDTO,
            final EngineerAvailability engineerAvailability) {
        engineerAvailability.setAvailabilityType(engineerAvailabilityDTO.getAvailabilityType());
        engineerAvailability.setStartTime(engineerAvailabilityDTO.getStartTime());
        engineerAvailability.setEndTime(engineerAvailabilityDTO.getEndTime());
        engineerAvailability.setNotes(engineerAvailabilityDTO.getNotes());
        engineerAvailability.setCreatedAt(engineerAvailabilityDTO.getCreatedAt());
        final User engineerUser = engineerAvailabilityDTO.getEngineerUser() == null ? null : userRepository.findById(engineerAvailabilityDTO.getEngineerUser())
                .orElseThrow(() -> new NotFoundException("engineerUser not found"));
        engineerAvailability.setEngineerUser(engineerUser);
        return engineerAvailability;
    }

    @EventListener(BeforeDeleteUser.class)
    public void on(final BeforeDeleteUser event) {
        final ReferencedException referencedException = new ReferencedException();
        final EngineerAvailability engineerUserEngineerAvailability = engineerAvailabilityRepository.findFirstByEngineerUserId(event.getId());
        if (engineerUserEngineerAvailability != null) {
            referencedException.setKey("user.engineerAvailability.engineerUser.referenced");
            referencedException.addParam(engineerUserEngineerAvailability.getId());
            throw referencedException;
        }
    }

}
