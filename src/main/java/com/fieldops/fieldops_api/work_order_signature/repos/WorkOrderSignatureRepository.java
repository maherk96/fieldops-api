package com.fieldops.fieldops_api.work_order_signature.repos;

import com.fieldops.fieldops_api.work_order_signature.domain.WorkOrderSignature;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderSignatureRepository extends JpaRepository<WorkOrderSignature, UUID> {

  WorkOrderSignature findFirstByWorkOrderId(UUID id);
}
