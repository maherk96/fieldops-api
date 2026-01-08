package com.fieldops.fieldops_api.attachment.repos;

import com.fieldops.fieldops_api.attachment.domain.Attachment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    Attachment findFirstByWorkOrderId(UUID id);

    Attachment findFirstByEventId(UUID id);

    Attachment findFirstByUploadedByUserId(UUID id);

}
