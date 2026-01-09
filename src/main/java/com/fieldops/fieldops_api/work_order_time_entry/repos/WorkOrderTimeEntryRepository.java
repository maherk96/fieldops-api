package com.fieldops.fieldops_api.work_order_time_entry.repos;

import com.fieldops.fieldops_api.work_order_time_entry.domain.WorkOrderTimeEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderTimeEntryRepository extends JpaRepository<WorkOrderTimeEntry, UUID> {

  WorkOrderTimeEntry findFirstByOrganizationId(UUID id);
}
