package com.fieldops.fieldops_api.organization.repos;

import com.fieldops.fieldops_api.organization.domain.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

  boolean existsBySubdomain(String subdomain);
}
