package com.fieldops.fieldops_api.audit_log.service;

import com.fieldops.fieldops_api.audit_log.domain.AuditLog;
import com.fieldops.fieldops_api.audit_log.model.AuditLogDTO;
import com.fieldops.fieldops_api.audit_log.repos.AuditLogRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
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
  private final OrganizationRepository organizationRepository;

  public AuditLogService(
      final AuditLogRepository auditLogRepository,
      final UserRepository userRepository,
      final OrganizationRepository organizationRepository) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
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
    auditLogDTO.setDateCreated(auditLog.getDateCreated());
    auditLogDTO.setDeviceId(auditLog.getDeviceId());
    auditLogDTO.setIpAddress(auditLog.getIpAddress());
    auditLogDTO.setLastUpdated(auditLog.getLastUpdated());
    auditLogDTO.setNewValues(auditLog.getNewValues());
    auditLogDTO.setOldValues(auditLog.getOldValues());
    auditLogDTO.setOperation(auditLog.getOperation());
    auditLogDTO.setRecordId(auditLog.getRecordId());
    auditLogDTO.setTableName(auditLog.getTableName());
    auditLogDTO.setUserAgent(auditLog.getUserAgent());
    auditLogDTO.setChangedByUser(
        auditLog.getChangedByUser() == null ? null : auditLog.getChangedByUser().getId());
    auditLogDTO.setOrganization(
        auditLog.getOrganization() == null ? null : auditLog.getOrganization().getId());
    return auditLogDTO;
  }

  private AuditLog mapToEntity(final AuditLogDTO auditLogDTO, final AuditLog auditLog) {
    auditLog.setDeviceId(auditLogDTO.getDeviceId());
    auditLog.setIpAddress(auditLogDTO.getIpAddress());
    auditLog.setNewValues(auditLogDTO.getNewValues());
    auditLog.setOldValues(auditLogDTO.getOldValues());
    auditLog.setOperation(auditLogDTO.getOperation());
    auditLog.setRecordId(auditLogDTO.getRecordId());
    auditLog.setTableName(auditLogDTO.getTableName());
    auditLog.setUserAgent(auditLogDTO.getUserAgent());
    final User changedByUser =
        auditLogDTO.getChangedByUser() == null
            ? null
            : userRepository
                .findById(auditLogDTO.getChangedByUser())
                .orElseThrow(() -> new NotFoundException("changedByUser not found"));
    auditLog.setChangedByUser(changedByUser);
    final Organization organization =
        auditLogDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(auditLogDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    auditLog.setOrganization(organization);
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

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final AuditLog organizationAuditLog =
        auditLogRepository.findFirstByOrganizationId(event.getId());
    if (organizationAuditLog != null) {
      referencedException.setKey("organization.auditLog.organization.referenced");
      referencedException.addParam(organizationAuditLog.getId());
      throw referencedException;
    }
  }
}
