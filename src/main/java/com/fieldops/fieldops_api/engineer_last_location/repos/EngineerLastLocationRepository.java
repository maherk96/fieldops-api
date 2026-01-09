package com.fieldops.fieldops_api.engineer_last_location.repos;

import com.fieldops.fieldops_api.engineer_last_location.domain.EngineerLastLocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerLastLocationRepository extends JpaRepository<EngineerLastLocation, Long> {

  EngineerLastLocation findFirstByOrganizationId(UUID id);
}
