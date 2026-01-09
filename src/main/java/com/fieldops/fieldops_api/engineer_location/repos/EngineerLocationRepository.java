package com.fieldops.fieldops_api.engineer_location.repos;

import com.fieldops.fieldops_api.engineer_location.domain.EngineerLocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerLocationRepository extends JpaRepository<EngineerLocation, UUID> {

  EngineerLocation findFirstByOrganizationId(UUID id);
}
