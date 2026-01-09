package com.fieldops.fieldops_api.work_order_event.repos;

import com.fieldops.fieldops_api.work_order_event.domain.WorkOrderEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderEventRepository extends JpaRepository<WorkOrderEvent, UUID> {

  WorkOrderEvent findFirstByOrganizationId(UUID id);
}
