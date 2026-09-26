package com.ecocity.esg.application.usecase;

import com.ecocity.esg.application.port.in.WasteCollectionUseCase;
import com.ecocity.esg.application.port.out.WasteCollectionRepositoryPort;
import com.ecocity.esg.domain.exception.ResourceNotFoundException;
import com.ecocity.esg.domain.model.WasteCollection;

import java.util.List;

public class WasteCollectionService implements WasteCollectionUseCase {

    private static final String RESOURCE_NAME = "WasteCollection";

    private final WasteCollectionRepositoryPort repositoryPort;

    public WasteCollectionService(WasteCollectionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public WasteCollection create(WasteCollection wasteCollection) {
        return repositoryPort.save(wasteCollection.forCreation());
    }

    @Override
    public WasteCollection createWithId(WasteCollection wasteCollection, String id) {
        WasteCollection created = wasteCollection.forCreation();
        return repositoryPort.save(created.toBuilder().id(id).build());
    }

    @Override
    public WasteCollection update(String id, WasteCollection wasteCollection) {
        wasteCollection.validate();
        WasteCollection existing = findById(id);
        return repositoryPort.save(existing.updateWith(wasteCollection));
    }

    @Override
    public WasteCollection update(String id, WasteCollection wasteCollection, long expectedVersion) {
        wasteCollection.validate();
        WasteCollection existing = findById(id);
        if (existing.getVersion() == null || existing.getVersion() != expectedVersion) {
            throw new com.ecocity.esg.domain.exception.StaleResourceException();
        }
        return repositoryPort.save(existing.updateWith(wasteCollection));
    }

    @Override
    public void delete(String id) {
        assertExists(id);
        repositoryPort.deleteById(id);
    }

    @Override
    public void delete(String id, long expectedVersion) {
        WasteCollection existing = findById(id);
        if (existing.getVersion() == null || existing.getVersion() != expectedVersion) {
            throw new com.ecocity.esg.domain.exception.StaleResourceException();
        }
        repositoryPort.delete(existing);
    }

    @Override
    public WasteCollection findById(String id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
    }

    @Override
    public List<WasteCollection> findAll(int page, int size) {
        return repositoryPort.findAll(page, size);
    }

    private void assertExists(String id) {
        if (!repositoryPort.existsById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
    }
}
