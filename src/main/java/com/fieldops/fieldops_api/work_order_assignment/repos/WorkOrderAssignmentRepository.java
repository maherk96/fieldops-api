package com.fieldops.fieldops_api.work_order_assignment.repos;

import com.fieldops.fieldops_api.work_order_assignment.domain.WorkOrderAssignment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderAssignmentRepository extends JpaRepository<WorkOrderAssignment, UUID> {

  WorkOrderAssignment findFirstByWorkOrderId(UUID id);

  WorkOrderAssignment findFirstByEngineerUserId(UUID id);
}
