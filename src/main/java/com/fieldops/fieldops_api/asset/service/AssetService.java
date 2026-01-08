package com.fieldops.fieldops_api.asset.service;

import com.fieldops.fieldops_api.asset.domain.Asset;
import com.fieldops.fieldops_api.asset.model.AssetDTO;
import com.fieldops.fieldops_api.asset.repos.AssetRepository;
import com.fieldops.fieldops_api.events.BeforeDeleteAsset;
import com.fieldops.fieldops_api.events.BeforeDeleteLocation;
import com.fieldops.fieldops_api.location.domain.Location;
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
public class AssetService {

    private final AssetRepository assetRepository;
    private final LocationRepository locationRepository;
    private final ApplicationEventPublisher publisher;

    public AssetService(final AssetRepository assetRepository,
            final LocationRepository locationRepository,
            final ApplicationEventPublisher publisher) {
        this.assetRepository = assetRepository;
        this.locationRepository = locationRepository;
        this.publisher = publisher;
    }

    public List<AssetDTO> findAll() {
        final List<Asset> assets = assetRepository.findAll(Sort.by("id"));
        return assets.stream()
                .map(asset -> mapToDTO(asset, new AssetDTO()))
                .toList();
    }

    public AssetDTO get(final UUID id) {
        return assetRepository.findById(id)
                .map(asset -> mapToDTO(asset, new AssetDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public UUID create(final AssetDTO assetDTO) {
        final Asset asset = new Asset();
        mapToEntity(assetDTO, asset);
        return assetRepository.save(asset).getId();
    }

    public void update(final UUID id, final AssetDTO assetDTO) {
        final Asset asset = assetRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(assetDTO, asset);
        assetRepository.save(asset);
    }

    public void delete(final UUID id) {
        final Asset asset = assetRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteAsset(id));
        assetRepository.delete(asset);
    }

    private AssetDTO mapToDTO(final Asset asset, final AssetDTO assetDTO) {
        assetDTO.setId(asset.getId());
        assetDTO.setAssetTag(asset.getAssetTag());
        assetDTO.setSerialNumber(asset.getSerialNumber());
        assetDTO.setManufacturer(asset.getManufacturer());
        assetDTO.setModel(asset.getModel());
        assetDTO.setVersion(asset.getVersion());
        assetDTO.setChangeVersion(asset.getChangeVersion());
        assetDTO.setCreatedAt(asset.getCreatedAt());
        assetDTO.setUpdatedAt(asset.getUpdatedAt());
        assetDTO.setLocation(asset.getLocation() == null ? null : asset.getLocation().getId());
        return assetDTO;
    }

    private Asset mapToEntity(final AssetDTO assetDTO, final Asset asset) {
        asset.setAssetTag(assetDTO.getAssetTag());
        asset.setSerialNumber(assetDTO.getSerialNumber());
        asset.setManufacturer(assetDTO.getManufacturer());
        asset.setModel(assetDTO.getModel());
        asset.setVersion(assetDTO.getVersion());
        asset.setChangeVersion(assetDTO.getChangeVersion());
        asset.setCreatedAt(assetDTO.getCreatedAt());
        asset.setUpdatedAt(assetDTO.getUpdatedAt());
        final Location location = assetDTO.getLocation() == null ? null : locationRepository.findById(assetDTO.getLocation())
                .orElseThrow(() -> new NotFoundException("location not found"));
        asset.setLocation(location);
        return asset;
    }

    public boolean serialNumberExists(final String serialNumber) {
        return assetRepository.existsBySerialNumber(serialNumber);
    }

    @EventListener(BeforeDeleteLocation.class)
    public void on(final BeforeDeleteLocation event) {
        final ReferencedException referencedException = new ReferencedException();
        final Asset locationAsset = assetRepository.findFirstByLocationId(event.getId());
        if (locationAsset != null) {
            referencedException.setKey("location.asset.location.referenced");
            referencedException.addParam(locationAsset.getId());
            throw referencedException;
        }
    }

}
