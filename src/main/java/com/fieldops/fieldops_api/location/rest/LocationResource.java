package com.fieldops.fieldops_api.location.rest;

import com.fieldops.fieldops_api.location.model.LocationDTO;
import com.fieldops.fieldops_api.location.service.LocationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/locations", produces = MediaType.APPLICATION_JSON_VALUE)
public class LocationResource {

    private final LocationService locationService;

    public LocationResource(final LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(locationService.get(id));
    }

    @PostMapping
    public ResponseEntity<UUID> createLocation(@RequestBody @Valid final LocationDTO locationDTO) {
        final UUID createdId = locationService.create(locationDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateLocation(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final LocationDTO locationDTO) {
        locationService.update(id, locationDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable(name = "id") final UUID id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
