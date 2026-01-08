package com.fieldops.fieldops_api.audit_log.service;

import com.fieldops.fieldops_api.audit_log.domain.AuditLog;
import com.fieldops.fieldops_api.audit_log.model.AuditLogDTO;
import com.fieldops.fieldops_api.audit_log.repos.AuditLogRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.user.domain.User;
import com.fieldops.fieldops_api.user.repos.UserRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;

  public AuditLogService(
      final AuditLogRepository auditLogRepository, final UserRepository userRepository) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
  }

  public List<AuditLogDTO> findAll() {
    final List<AuditLog> auditLogs = auditLogRepository.findAll(Sort.by("id"));
    return auditLogs.stream().map(auditLog -> mapToDTO(auditLog, new AuditLogDTO())).toList();
  }

  public AuditLogDTO get(final UUID id) {
    return auditLogRepository
        .findById(id)
        .map(auditLog -> mapToDTO(auditLog, new AuditLogDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final AuditLogDTO auditLogDTO) {
    final AuditLog auditLog = new AuditLog();
    mapToEntity(auditLogDTO, auditLog);
    return auditLogRepository.save(auditLog).getId();
  }

  public void update(final UUID id, final AuditLogDTO auditLogDTO) {
    final AuditLog auditLog = auditLogRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(auditLogDTO, auditLog);
    auditLogRepository.save(auditLog);
  }

  public void delete(final UUID id) {
    final AuditLog auditLog = auditLogRepository.findById(id).orElseThrow(NotFoundException::new);
    auditLogRepository.delete(auditLog);
  }

  private AuditLogDTO mapToDTO(final AuditLog auditLog, final AuditLogDTO auditLogDTO) {
    auditLogDTO.setId(auditLog.getId());
    auditLogDTO.setTableName(auditLog.getTableName());
    auditLogDTO.setRecordId(auditLog.getRecordId());
    auditLogDTO.setOperation(auditLog.getOperation());
    auditLogDTO.setOldValues(auditLog.getOldValues());
    auditLogDTO.setNewValues(auditLog.getNewValues());
    auditLogDTO.setChangedAt(auditLog.getChangedAt());
    auditLogDTO.setIpAddress(auditLog.getIpAddress());
    auditLogDTO.setUserAgent(auditLog.getUserAgent());
    auditLogDTO.setDeviceId(auditLog.getDeviceId());
    auditLogDTO.setChangedByUser(
        auditLog.getChangedByUser() == null ? null : auditLog.getChangedByUser().getId());
    return auditLogDTO;
  }

  private AuditLog mapToEntity(final AuditLogDTO auditLogDTO, final AuditLog auditLog) {
    auditLog.setTableName(auditLogDTO.getTableName());
    auditLog.setRecordId(auditLogDTO.getRecordId());
    auditLog.setOperation(auditLogDTO.getOperation());
    auditLog.setOldValues(auditLogDTO.getOldValues());
    auditLog.setNewValues(auditLogDTO.getNewValues());
    auditLog.setChangedAt(auditLogDTO.getChangedAt());
    auditLog.setIpAddress(auditLogDTO.getIpAddress());
    auditLog.setUserAgent(auditLogDTO.getUserAgent());
    auditLog.setDeviceId(auditLogDTO.getDeviceId());
    final User changedByUser =
        auditLogDTO.getChangedByUser() == null
            ? null
            : userRepository
                .findById(auditLogDTO.getChangedByUser())
                .orElseThrow(() -> new NotFoundException("changedByUser not found"));
    auditLog.setChangedByUser(changedByUser);
    return auditLog;
  }

  @EventListener(BeforeDeleteUser.class)
  public void on(final BeforeDeleteUser event) {
    final ReferencedException referencedException = new ReferencedException();
    final AuditLog changedByUserAuditLog =
        auditLogRepository.findFirstByChangedByUserId(event.getId());
    if (changedByUserAuditLog != null) {
      referencedException.setKey("user.auditLog.changedByUser.referenced");
      referencedException.addParam(changedByUserAuditLog.getId());
      throw referencedException;
    }
  }
}
