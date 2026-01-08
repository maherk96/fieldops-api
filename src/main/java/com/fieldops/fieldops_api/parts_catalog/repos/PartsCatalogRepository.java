package com.fieldops.fieldops_api.parts_catalog.repos;

import com.fieldops.fieldops_api.parts_catalog.domain.PartsCatalog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PartsCatalogRepository extends JpaRepository<PartsCatalog, UUID> {
}
