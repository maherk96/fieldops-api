package com.fieldops.fieldops_api.customer.service;

import com.fieldops.fieldops_api.customer.domain.Customer;
import com.fieldops.fieldops_api.customer.model.CustomerDTO;
import com.fieldops.fieldops_api.customer.repos.CustomerRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteOrganization;
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
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final OrganizationRepository organizationRepository;

  public CustomerService(
      final CustomerRepository customerRepository,
      final OrganizationRepository organizationRepository) {
    this.customerRepository = customerRepository;
    this.organizationRepository = organizationRepository;
  }

  public List<CustomerDTO> findAll() {
    final List<Customer> customers = customerRepository.findAll(Sort.by("id"));
    return customers.stream().map(customer -> mapToDTO(customer, new CustomerDTO())).toList();
  }

  public CustomerDTO get(final UUID id) {
    return customerRepository
        .findById(id)
        .map(customer -> mapToDTO(customer, new CustomerDTO()))
        .orElseThrow(NotFoundException::new);
  }

  public UUID create(final CustomerDTO customerDTO) {
    final Customer customer = new Customer();
    mapToEntity(customerDTO, customer);
    return customerRepository.save(customer).getId();
  }

  public void update(final UUID id, final CustomerDTO customerDTO) {
    final Customer customer = customerRepository.findById(id).orElseThrow(NotFoundException::new);
    mapToEntity(customerDTO, customer);
    customerRepository.save(customer);
  }

  public void delete(final UUID id) {
    final Customer customer = customerRepository.findById(id).orElseThrow(NotFoundException::new);
    customerRepository.delete(customer);
  }

  private CustomerDTO mapToDTO(final Customer customer, final CustomerDTO customerDTO) {
    customerDTO.setId(customer.getId());
    customerDTO.setChangeVersion(customer.getChangeVersion());
    customerDTO.setDateCreated(customer.getDateCreated());
    customerDTO.setExternalRef(customer.getExternalRef());
    customerDTO.setLastUpdated(customer.getLastUpdated());
    customerDTO.setName(customer.getName());
    customerDTO.setPhone(customer.getPhone());
    customerDTO.setUpdatedAt(customer.getUpdatedAt());
    customerDTO.setVersion(customer.getVersion());
    customerDTO.setOrganization(
        customer.getOrganization() == null ? null : customer.getOrganization().getId());
    return customerDTO;
  }

  private Customer mapToEntity(final CustomerDTO customerDTO, final Customer customer) {
    customer.setChangeVersion(customerDTO.getChangeVersion());
    customer.setExternalRef(customerDTO.getExternalRef());
    customer.setName(customerDTO.getName());
    customer.setPhone(customerDTO.getPhone());
    customer.setUpdatedAt(customerDTO.getUpdatedAt());
    customer.setVersion(customerDTO.getVersion());
    final Organization organization =
        customerDTO.getOrganization() == null
            ? null
            : organizationRepository
                .findById(customerDTO.getOrganization())
                .orElseThrow(() -> new NotFoundException("organization not found"));
    customer.setOrganization(organization);
    return customer;
  }

  @EventListener(BeforeDeleteOrganization.class)
  public void on(final BeforeDeleteOrganization event) {
    final ReferencedException referencedException = new ReferencedException();
    final Customer organizationCustomer =
        customerRepository.findFirstByOrganizationId(event.getId());
    if (organizationCustomer != null) {
      referencedException.setKey("organization.customer.organization.referenced");
      referencedException.addParam(organizationCustomer.getId());
      throw referencedException;
    }
  }
}
