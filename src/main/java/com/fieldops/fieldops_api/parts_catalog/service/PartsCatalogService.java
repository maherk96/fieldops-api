package com.fieldops.fieldops_api.parts_catalog.service;

import com.fieldops.fieldops_api.events.BeforeDeletePartsCatalog;
import com.fieldops.fieldops_api.parts_catalog.domain.PartsCatalog;
import com.fieldops.fieldops_api.parts_catalog.model.PartsCatalogDTO;
import com.fieldops.fieldops_api.parts_catalog.repos.PartsCatalogRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PartsCatalogService {

  private final PartsCatalogRepository partsCatalogRepository;
  private final ApplicationEventPublisher publisher;

  public PartsCatalogService(
      final PartsCatalogRepository partsCatalogRepository,
      final ApplicationEventPublisher publisher) {
    this.partsCatalogRepository = partsCatalogRepository;
    this.publisher = publisher;
  }

  public List<PartsCatalogDTO> findAll() {
    final List<PartsCatalog> partsCatalogs = partsCatalogRepository.findAll(Sort.by("id"));
    return partsCatalogs.stream()
        .map(partsCatalog -> mapToDTO(partsCatalog, new PartsCatalogDTO()))
        .toList();
  }

  public PartsCatalogDTO get(final UUID id) {
    return partsCatalogRepository
        .findById(id)
        .map(partsCatalog -> mapToDTO(partsCatalog, new PartsCatalogDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final PartsCatalogDTO partsCatalogDTO) {
    final PartsCatalog partsCatalog = new PartsCatalog();
    mapToEntity(partsCatalogDTO, partsCatalog);
    return partsCatalogRepository.save(partsCatalog).getId();
  }

  public void update(final UUID id, final PartsCatalogDTO partsCatalogDTO) {
    final PartsCatalog partsCatalog =
        partsCatalogRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(partsCatalogDTO, partsCatalog);
    partsCatalogRepository.save(partsCatalog);
  }

  public void delete(final UUID id) {
    final PartsCatalog partsCatalog =
        partsCatalogRepository.findById(id).orElseThrow(NotFoundException::new);
    publisher.publishEvent(new BeforeDeletePartsCatalog(id));
    partsCatalogRepository.delete(partsCatalog);
  }

  private PartsCatalogDTO mapToDTO(
      final PartsCatalog partsCatalog, final PartsCatalogDTO partsCatalogDTO) {
    partsCatalogDTO.setId(partsCatalog.getId());
    partsCatalogDTO.setPartNumber(partsCatalog.getPartNumber());
    partsCatalogDTO.setDescription(partsCatalog.getDescription());
    partsCatalogDTO.setUnitPrice(partsCatalog.getUnitPrice());
    partsCatalogDTO.setActive(partsCatalog.getActive());
    partsCatalogDTO.setVersion(partsCatalog.getVersion());
    partsCatalogDTO.setChangeVersion(partsCatalog.getChangeVersion());
    partsCatalogDTO.setCreatedAt(partsCatalog.getCreatedAt());
    partsCatalogDTO.setUpdatedAt(partsCatalog.getUpdatedAt());
    return partsCatalogDTO;
  }

  private PartsCatalog mapToEntity(
      final PartsCatalogDTO partsCatalogDTO, final PartsCatalog partsCatalog) {
    partsCatalog.setPartNumber(partsCatalogDTO.getPartNumber());
    partsCatalog.setDescription(partsCatalogDTO.getDescription());
    partsCatalog.setUnitPrice(partsCatalogDTO.getUnitPrice());
    partsCatalog.setActive(partsCatalogDTO.getActive());
    partsCatalog.setVersion(partsCatalogDTO.getVersion());
    partsCatalog.setChangeVersion(partsCatalogDTO.getChangeVersion());
    partsCatalog.setCreatedAt(partsCatalogDTO.getCreatedAt());
    partsCatalog.setUpdatedAt(partsCatalogDTO.getUpdatedAt());
    return partsCatalog;
  }
}
