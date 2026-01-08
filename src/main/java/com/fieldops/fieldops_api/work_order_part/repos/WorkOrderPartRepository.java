package com.fieldops.fieldops_api.work_order_part.repos;

import com.fieldops.fieldops_api.work_order_part.domain.WorkOrderPart;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WorkOrderPartRepository extends JpaRepository<WorkOrderPart, UUID> {

    WorkOrderPart findFirstByWorkOrderId(UUID id);

    WorkOrderPart findFirstByPartId(UUID id);

    WorkOrderPart findFirstByRecordedByUserId(UUID id);

}
