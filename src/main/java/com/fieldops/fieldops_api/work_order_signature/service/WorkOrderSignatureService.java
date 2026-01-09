package com.fieldops.fieldops_api.work_order_signature.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import com.fieldops.fieldops_api.work_order_signature.domain.WorkOrderSignature;
import com.fieldops.fieldops_api.work_order_signature.model.WorkOrderSignatureDTO;
import com.fieldops.fieldops_api.work_order_signature.repos.WorkOrderSignatureRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderSignatureService {

  private final WorkOrderSignatureRepository workOrderSignatureRepository;
  private final OrganizationRepository organizationRepository;

  public WorkOrderSignatureService(
      final WorkOrderSignatureRepository workOrderSignatureRepository,
      final OrganizationRepository organizationRepository) {
    this.workOrderSignatureRepository = workOrderSignatureRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<WorkOrderSignatureDTO> findAll() {
    final List<WorkOrderSignature> workOrderSignatures =
        workOrderSignatureRepository.findAll(Sort.by("id"));
    return workOrderSignatures.stream()
        .map(workOrderSignature -> mapToDTO(workOrderSignature, new WorkOrderSignatureDTO()))
        .toList();
  }

  public WorkOrderSignatureDTO get(final UUID id) {
    return workOrderSignatureRepository
        .findById(id)
        .map(workOrderSignature -> mapToDTO(workOrderSignature, new WorkOrderSignatureDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final WorkOrderSignatureDTO workOrderSignatureDTO) {
    final WorkOrderSignature workOrderSignature = new WorkOrderSignature();
    mapToEntity(workOrderSignatureDTO, workOrderSignature);
    return workOrderSignatureRepository.save(workOrderSignature).getId();
  }

  public void update(final UUID id, final WorkOrderSignatureDTO workOrderSignatureDTO) {
    final WorkOrderSignature workOrderSignature =
        workOrderSignatureRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(workOrderSignatureDTO, workOrderSignature);
    workOrderSignatureRepository.save(workOrderSignature);
  }

  public void delete(final UUID id) {
    final WorkOrderSignature workOrderSignature =
        workOrderSignatureRepository.findById(id).orElseThrow(NotFoundException::new);
    workOrderSignatureRepository.delete(workOrderSignature);
  }

  private WorkOrderSignatureDTO mapToDTO(
      final WorkOrderSignature workOrderSignature,
      final WorkOrderSignatureDTO workOrderSignatureDTO) {
    workOrderSignatureDTO.setId(workOrderSignature.getId());
    workOrderSignatureDTO.setDateCreated(workOrderSignature.getDateCreated());
    workOrderSignatureDTO.setLastUpdated(workOrderSignature.getLastUpdated());
    workOrderSignatureDTO.setSignatureType(workOrderSignature.getSignatureType());
    workOrderSignatureDTO.setSignedAt(workOrderSignature.getSignedAt());
    workOrderSignatureDTO.setSignerName(workOrderSignature.getSignerName());
    workOrderSignatureDTO.setStorageKey(workOrderSignature.getStorageKey());
    workOrderSignatureDTO.setWorkOrderId(workOrderSignature.getWorkOrderId());
    workOrderSignatureDTO.setOrganization(
        workOrderSignature.getOrganization() == null
            ? null
            : workOrderSignature.getOrganization().getId());
    return workOrderSignatureDTO;
  }

  private WorkOrderSignature mapToEntity(
      final WorkOrderSignatureDTO workOrderSignatureDTO,
      final WorkOrderSignature workOrderSignature) {

    workOrderSignature.setSignatureType(workOrderSignatureDTO.getSignatureType());
    workOrderSignature.setSignedAt(workOrderSignatureDTO.getSignedAt());
    workOrderSignature.setSignerName(workOrderSignatureDTO.getSignerName());
    workOrderSignature.setStorageKey(workOrderSignatureDTO.getStorageKey());
    workOrderSignature.setWorkOrderId(workOrderSignatureDTO.getWorkOrderId());
    final Organization organization =
        workOrderSignatureDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(workOrderSignatureDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    workOrderSignature.setOrganization(organization);
    return workOrderSignature;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final WorkOrderSignature organizationWorkOrderSignature =
        workOrderSignatureRepository.findFirstByOrganizationId(event.getId());
    if (organizationWorkOrderSignature != null) {
      referencedException.setKey("organization.workOrderSignature.organization.referenced");
      referencedException.addParam(organizationWorkOrderSignature.getId());
      throw referencedException;
    }
  }
}
