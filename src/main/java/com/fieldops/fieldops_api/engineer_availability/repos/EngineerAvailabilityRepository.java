package com.fieldops.fieldops_api.engineer_availability.repos;

import com.fieldops.fieldops_api.engineer_availability.domain.EngineerAvailability;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerAvailabilityRepository extends JpaRepository<EngineerAvailability, UUID> {

  EngineerAvailability findFirstByEngineerUserId(UUID id);
}
