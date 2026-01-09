package com.fieldops.fieldops_api.work_order.repos;

import com.fieldops.fieldops_api.work_order.domain.WorkOrder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

  WorkOrder findFirstByOrganizationId(UUID id);
}
