package com.fieldops.fieldops_api.customer.service;

import com.fieldops.fieldops_api.customer.domain.Customer;
import com.fieldops.fieldops_api.customer.model.CustomerDTO;
import com.fieldops.fieldops_api.customer.repos.CustomerRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteCustomer;
import com.fieldops.fieldops_api.util.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final ApplicationEventPublisher publisher;

  public CustomerService(
      final CustomerRepository customerRepository, final ApplicationEventPublisher publisher) {
    this.customerRepository = customerRepository;
    this.publisher = publisher;
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
    publisher.publishEvent(new BeforeDeleteCustomer(id));
    customerRepository.delete(customer);
  }

  private CustomerDTO mapToDTO(final Customer customer, final CustomerDTO customerDTO) {
    customerDTO.setId(customer.getId());
    customerDTO.setName(customer.getName());
    customerDTO.setExternalRef(customer.getExternalRef());
    customerDTO.setPhone(customer.getPhone());
    customerDTO.setVersion(customer.getVersion());
    customerDTO.setChangeVersion(customer.getChangeVersion());
    customerDTO.setCreatedAt(customer.getCreatedAt());
    customerDTO.setUpdatedAt(customer.getUpdatedAt());
    return customerDTO;
  }

  private Customer mapToEntity(final CustomerDTO customerDTO, final Customer customer) {
    customer.setName(customerDTO.getName());
    customer.setExternalRef(customerDTO.getExternalRef());
    customer.setPhone(customerDTO.getPhone());
    customer.setVersion(customerDTO.getVersion());
    customer.setChangeVersion(customerDTO.getChangeVersion());
    customer.setCreatedAt(customerDTO.getCreatedAt());
    customer.setUpdatedAt(customerDTO.getUpdatedAt());
    return customer;
  }
}
