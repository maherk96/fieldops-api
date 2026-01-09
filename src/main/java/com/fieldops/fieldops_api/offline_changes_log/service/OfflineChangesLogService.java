package com.fieldops.fieldops_api.offline_changes_log.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.offline_changes_log.domain.OfflineChangesLog;
import com.fieldops.fieldops_api.offline_changes_log.model.OfflineChangesLogDTO;
import com.fieldops.fieldops_api.offline_changes_log.repos.OfflineChangesLogRepository;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OfflineChangesLogService {

  private final OfflineChangesLogRepository offlineChangesLogRepository;
  private final OrganizationRepository organizationRepository;

  public OfflineChangesLogService(
      final OfflineChangesLogRepository offlineChangesLogRepository,
      final OrganizationRepository organizationRepository) {
    this.offlineChangesLogRepository = offlineChangesLogRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<OfflineChangesLogDTO> findAll() {
    final List<OfflineChangesLog> offlineChangesLogs =
        offlineChangesLogRepository.findAll(Sort.by("id"));
    return offlineChangesLogs.stream()
        .map(offlineChangesLog -> mapToDTO(offlineChangesLog, new OfflineChangesLogDTO()))
        .toList();
  }

  public OfflineChangesLogDTO get(final UUID id) {
    return offlineChangesLogRepository
        .findById(id)
        .map(offlineChangesLog -> mapToDTO(offlineChangesLog, new OfflineChangesLogDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final OfflineChangesLogDTO offlineChangesLogDTO) {
    final OfflineChangesLog offlineChangesLog = new OfflineChangesLog();
    mapToEntity(offlineChangesLogDTO, offlineChangesLog);
    return offlineChangesLogRepository.save(offlineChangesLog).getId();
  }

  public void update(final UUID id, final OfflineChangesLogDTO offlineChangesLogDTO) {
    final OfflineChangesLog offlineChangesLog =
        offlineChangesLogRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(offlineChangesLogDTO, offlineChangesLog);
    offlineChangesLogRepository.save(offlineChangesLog);
  }

  public void delete(final UUID id) {
    final OfflineChangesLog offlineChangesLog =
        offlineChangesLogRepository.findById(id).orElseThrow(NotFoundException::new);
    offlineChangesLogRepository.delete(offlineChangesLog);
  }

  private OfflineChangesLogDTO mapToDTO(
      final OfflineChangesLog offlineChangesLog, final OfflineChangesLogDTO offlineChangesLogDTO) {
    offlineChangesLogDTO.setId(offlineChangesLog.getId());
    offlineChangesLogDTO.setChanges(offlineChangesLog.getChanges());
    offlineChangesLogDTO.setDateCreated(offlineChangesLog.getDateCreated());
    offlineChangesLogDTO.setDeviceId(offlineChangesLog.getDeviceId());
    offlineChangesLogDTO.setLastUpdated(offlineChangesLog.getLastUpdated());
    offlineChangesLogDTO.setOperation(offlineChangesLog.getOperation());
    offlineChangesLogDTO.setRecordId(offlineChangesLog.getRecordId());
    offlineChangesLogDTO.setSyncedAt(offlineChangesLog.getSyncedAt());
    offlineChangesLogDTO.setTableName(offlineChangesLog.getTableName());
    offlineChangesLogDTO.setUserId(offlineChangesLog.getUserId());
    offlineChangesLogDTO.setOrganization(
        offlineChangesLog.getOrganization() == null
            ? null
            : offlineChangesLog.getOrganization().getId());
    return offlineChangesLogDTO;
  }

  private OfflineChangesLog mapToEntity(
      final OfflineChangesLogDTO offlineChangesLogDTO, final OfflineChangesLog offlineChangesLog) {
    offlineChangesLog.setChanges(offlineChangesLogDTO.getChanges());
    offlineChangesLog.setDeviceId(offlineChangesLogDTO.getDeviceId());
    offlineChangesLog.setOperation(offlineChangesLogDTO.getOperation());
    offlineChangesLog.setRecordId(offlineChangesLogDTO.getRecordId());
    offlineChangesLog.setSyncedAt(offlineChangesLogDTO.getSyncedAt());
    offlineChangesLog.setTableName(offlineChangesLogDTO.getTableName());
    offlineChangesLog.setUserId(offlineChangesLogDTO.getUserId());
    final Organization organization =
        offlineChangesLogDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(offlineChangesLogDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    offlineChangesLog.setOrganization(organization);
    return offlineChangesLog;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final OfflineChangesLog organizationOfflineChangesLog =
        offlineChangesLogRepository.findFirstByOrganizationId(event.getId());
    if (organizationOfflineChangesLog != null) {
      referencedException.setKey("organization.offlineChangesLog.organization.referenced");
      referencedException.addParam(organizationOfflineChangesLog.getId());
      throw referencedException;
    }
  }
}
