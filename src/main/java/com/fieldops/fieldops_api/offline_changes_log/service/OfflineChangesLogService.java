package com.fieldops.fieldops_api.offline_changes_log.service;

import com.fieldops.fieldops_api.events.BeforeDeleteUser;
import com.fieldops.fieldops_api.offline_changes_log.domain.OfflineChangesLog;
import com.fieldops.fieldops_api.offline_changes_log.model.OfflineChangesLogDTO;
import com.fieldops.fieldops_api.offline_changes_log.repos.OfflineChangesLogRepository;
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
public class OfflineChangesLogService {

  private final OfflineChangesLogRepository offlineChangesLogRepository;
  private final UserRepository userRepository;

  public OfflineChangesLogService(
      final OfflineChangesLogRepository offlineChangesLogRepository,
      final UserRepository userRepository) {
    this.offlineChangesLogRepository = offlineChangesLogRepository;
    this.userRepository = userRepository;
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
    offlineChangesLogDTO.setDeviceId(offlineChangesLog.getDeviceId());
    offlineChangesLogDTO.setTableName(offlineChangesLog.getTableName());
    offlineChangesLogDTO.setRecordId(offlineChangesLog.getRecordId());
    offlineChangesLogDTO.setOperation(offlineChangesLog.getOperation());
    offlineChangesLogDTO.setChanges(offlineChangesLog.getChanges());
    offlineChangesLogDTO.setSyncedAt(offlineChangesLog.getSyncedAt());
    offlineChangesLogDTO.setCreatedAt(offlineChangesLog.getCreatedAt());
    offlineChangesLogDTO.setUser(
        offlineChangesLog.getUser() == null ? null : offlineChangesLog.getUser().getId());
    return offlineChangesLogDTO;
  }

  private OfflineChangesLog mapToEntity(
      final OfflineChangesLogDTO offlineChangesLogDTO, final OfflineChangesLog offlineChangesLog) {
    offlineChangesLog.setDeviceId(offlineChangesLogDTO.getDeviceId());
    offlineChangesLog.setTableName(offlineChangesLogDTO.getTableName());
    offlineChangesLog.setRecordId(offlineChangesLogDTO.getRecordId());
    offlineChangesLog.setOperation(offlineChangesLogDTO.getOperation());
    offlineChangesLog.setChanges(offlineChangesLogDTO.getChanges());
    offlineChangesLog.setSyncedAt(offlineChangesLogDTO.getSyncedAt());
    offlineChangesLog.setCreatedAt(offlineChangesLogDTO.getCreatedAt());
    final User user =
        offlineChangesLogDTO.getUser() == null
            ? null
            : userRepository
                .findById(offlineChangesLogDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
    offlineChangesLog.setUser(user);
    return offlineChangesLog;
  }

  @EventListener(BeforeDeleteUser.class)
  public void on(final BeforeDeleteUser event) {
    final ReferencedException referencedException = new ReferencedException();
    final OfflineChangesLog userOfflineChangesLog =
        offlineChangesLogRepository.findFirstByUserId(event.getId());
    if (userOfflineChangesLog != null) {
      referencedException.setKey("user.offlineChangesLog.user.referenced");
      referencedException.addParam(userOfflineChangesLog.getId());
      throw referencedException;
    }
  }
}
