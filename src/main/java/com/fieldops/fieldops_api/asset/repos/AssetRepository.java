package com.fieldops.fieldops_api.asset.repos;

import com.fieldops.fieldops_api.asset.domain.Asset;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

  Asset findFirstByLocationId(UUID id);

  boolean existsBySerialNumber(String serialNumber);
}
