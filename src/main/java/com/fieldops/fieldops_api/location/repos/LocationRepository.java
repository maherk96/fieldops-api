package com.fieldops.fieldops_api.location.repos;

import com.fieldops.fieldops_api.location.domain.Location;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, UUID> {

  Location findFirstByCustomerId(UUID id);
}
