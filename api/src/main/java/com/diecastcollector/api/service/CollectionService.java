package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Collection;
import com.diecastcollector.api.dto.CollectionRequest;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.CollectionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public List<Collection> findAll() {
        return collectionRepository.findAllByOrderByNameAsc();
    }

    public Collection getById(Long id) {
        return collectionRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Collection create(CollectionRequest request) {
        return collectionRepository.save(new Collection(request.name(), request.year()));
    }

    @Transactional
    public Collection update(Long id, CollectionRequest request) {
        Collection collection = getById(id);
        collection.setName(request.name());
        collection.setYear(request.year());
        return collectionRepository.save(collection);
    }

    @Transactional
    public void delete(Long id) {
        collectionRepository.delete(getById(id));
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Collection " + id + " not found");
    }
}
