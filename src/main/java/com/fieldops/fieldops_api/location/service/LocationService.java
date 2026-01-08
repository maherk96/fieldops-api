package com.fieldops.fieldops_api.location.service;

import com.fieldops.fieldops_api.customer.domain.Customer;
import com.fieldops.fieldops_api.customer.repos.CustomerRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteCustomer;
import com.fieldops.fieldops_api.events.BeforeDeleteLocation;
import com.fieldops.fieldops_api.location.domain.Location;
import com.fieldops.fieldops_api.location.model.LocationDTO;
import com.fieldops.fieldops_api.location.repos.LocationRepository;
import com.fieldops.fieldops_api.util.NotFoundException;
import com.fieldops.fieldops_api.util.ReferencedException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher publisher;

    public LocationService(final LocationRepository locationRepository,
            final CustomerRepository customerRepository,
            final ApplicationEventPublisher publisher) {
        this.locationRepository = locationRepository;
        this.customerRepository = customerRepository;
        this.publisher = publisher;
    }

    public List<LocationDTO> findAll() {
        final List<Location> locations = locationRepository.findAll(Sort.by("id"));
        return locations.stream()
                .map(location -> mapToDTO(location, new LocationDTO()))
                .toList();
    }

    public LocationDTO get(final UUID id) {
        return locationRepository.findById(id)
                .map(location -> mapToDTO(location, new LocationDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final LocationDTO locationDTO) {
        final Location location = new Location();
        mapToEntity(locationDTO, location);
        return locationRepository.save(location).getId();
    }

    public void update(final UUID id, final LocationDTO locationDTO) {
        final Location location = locationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(locationDTO, location);
        locationRepository.save(location);
    }

    public void delete(final UUID id) {
        final Location location = locationRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteLocation(id));
        locationRepository.delete(location);
    }

    private LocationDTO mapToDTO(final Location location, final LocationDTO locationDTO) {
        locationDTO.setId(location.getId());
        locationDTO.setName(location.getName());
        locationDTO.setAddressLine1(location.getAddressLine1());
        locationDTO.setAddressLine2(location.getAddressLine2());
        locationDTO.setCity(location.getCity());
        locationDTO.setRegion(location.getRegion());
        locationDTO.setPostcode(location.getPostcode());
        locationDTO.setCountry(location.getCountry());
        locationDTO.setContactPhone(location.getContactPhone());
        locationDTO.setLat(location.getLat());
        locationDTO.setLng(location.getLng());
        locationDTO.setVersion(location.getVersion());
        locationDTO.setChangeVersion(location.getChangeVersion());
        locationDTO.setCreatedAt(location.getCreatedAt());
        locationDTO.setUpdatedAt(location.getUpdatedAt());
        locationDTO.setCustomer(location.getCustomer() == null ? null : location.getCustomer().getId());
        return locationDTO;
    }

    private Location mapToEntity(final LocationDTO locationDTO, final Location location) {
        location.setName(locationDTO.getName());
        location.setAddressLine1(locationDTO.getAddressLine1());
        location.setAddressLine2(locationDTO.getAddressLine2());
        location.setCity(locationDTO.getCity());
        location.setRegion(locationDTO.getRegion());
        location.setPostcode(locationDTO.getPostcode());
        location.setCountry(locationDTO.getCountry());
        location.setContactPhone(locationDTO.getContactPhone());
        location.setLat(locationDTO.getLat());
        location.setLng(locationDTO.getLng());
        location.setVersion(locationDTO.getVersion());
        location.setChangeVersion(locationDTO.getChangeVersion());
        location.setCreatedAt(locationDTO.getCreatedAt());
        location.setUpdatedAt(locationDTO.getUpdatedAt());
        final Customer customer = locationDTO.getCustomer() == null ? null : customerRepository.findById(locationDTO.getCustomer())
                .orElseThrow(() -> new NotFoundException("customer not found"));
        location.setCustomer(customer);
        return location;
    }

    @EventListener(BeforeDeleteCustomer.class)
    public void on(final BeforeDeleteCustomer event) {
        final ReferencedException referencedException = new ReferencedException();
        final Location customerLocation = locationRepository.findFirstByCustomerId(event.getId());
        if (customerLocation != null) {
            referencedException.setKey("customer.location.customer.referenced");
            referencedException.addParam(customerLocation.getId());
            throw referencedException;
        }
    }

}
