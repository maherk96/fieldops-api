package com.fieldops.fieldops_api.organization.service;

import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
import com.fieldops.fieldops_api.organization.domain.Organization;
import com.fieldops.fieldops_api.organization.model.OrganizationDTO;
import com.fieldops.fieldops_api.organization.repos.OrganizationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final ApplicationEventPublisher publisher;

  public OrganizationService(
      final OrganizationRepository organizationRepository,
      final ApplicationEventPublisher publisher) {
    this.organizationRepository = organizationRepository;
    this.publisher = publisher;
  }

  public List<OrganizationDTO> findAll() {
    final List<Organization> organizations = organizationRepository.findAll(Sort.by("id"));
    return organizations.stream()
        .map(organization -> mapToDTO(organization, new OrganizationDTO()))
        .toList();
  }

  public OrganizationDTO get(final UUID id) {
    return organizationRepository
        .findById(id)
        .map(organization -> mapToDTO(organization, new OrganizationDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final OrganizationDTO organizationDTO) {
    final Organization organization = new Organization();
    mapToEntity(organizationDTO, organization);
    return organizationRepository.save(organization).getId();
  }

  public void update(final UUID id, final OrganizationDTO organizationDTO) {
    final Organization organization =
        organizationRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(organizationDTO, organization);
    organizationRepository.save(organization);
  }

  public void delete(final UUID id) {
    final Organization organization =
        organizationRepository.findById(id).orElseThrow(NotFoundException::new);
    publisher.publishEvent(new BeforeDeleteOrganization(id));
    organizationRepository.delete(organization);
  }

  private OrganizationDTO mapToDTO(
      final Organization organization, final OrganizationDTO organizationDTO) {
    organizationDTO.setId(organization.getId());
    organizationDTO.setName(organization.getName());
    organizationDTO.setSubdomain(organization.getSubdomain());
    organizationDTO.setLogoUrl(organization.getLogoUrl());
    organizationDTO.setSettings(organization.getSettings());
    organizationDTO.setSubscriptionStatus(organization.getSubscriptionStatus());
    organizationDTO.setSubscriptionPlan(organization.getSubscriptionPlan());
    organizationDTO.setMaxEngineers(organization.getMaxEngineers());
    organizationDTO.setMaxWorkOrdersPerMonth(organization.getMaxWorkOrdersPerMonth());
    organizationDTO.setTrialEndsAt(organization.getTrialEndsAt());
    organizationDTO.setDateCreated(organization.getDateCreated());
    organizationDTO.setLastUpdated(organization.getLastUpdated());
    return organizationDTO;
  }

  private Organization mapToEntity(
      final OrganizationDTO organizationDTO, final Organization organization) {
    organization.setName(organizationDTO.getName());
    organization.setSubdomain(organizationDTO.getSubdomain());
    organization.setLogoUrl(organizationDTO.getLogoUrl());
    organization.setSettings(organizationDTO.getSettings());
    organization.setSubscriptionStatus(organizationDTO.getSubscriptionStatus());
    organization.setSubscriptionPlan(organizationDTO.getSubscriptionPlan());
    organization.setMaxEngineers(organizationDTO.getMaxEngineers());
    organization.setMaxWorkOrdersPerMonth(organizationDTO.getMaxWorkOrdersPerMonth());
    organization.setTrialEndsAt(organizationDTO.getTrialEndsAt());
    return organization;
  }

  public boolean subdomainExists(final String subdomain) {
    return organizationRepository.existsBySubdomain(subdomain);
  }
}
