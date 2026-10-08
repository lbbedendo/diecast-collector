package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Automaker;
import com.diecastcollector.api.domain.Brand;
import com.diecastcollector.api.domain.Model;
import com.diecastcollector.api.domain.Series;
import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.dto.ModelRequest;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.ModelRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All reads/writes are scoped to the calling user's own collection (owner_id) — this is a
 * personal collection tracker, not a shared catalog, so one user must never be able to see or
 * edit another user's models.
 */
@Service
public class ModelService {

    private final ModelRepository modelRepository;

    public ModelService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public List<Model> findAllForOwner(Long ownerId) {
        return modelRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public Model getForOwner(Long id, Long ownerId) {
        return modelRepository.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Model create(Long ownerId, ModelRequest request) {
        Model model = new Model();
        model.setOwner(new User(ownerId));
        applyRequest(model, request);
        return modelRepository.save(model);
    }

    @Transactional
    public Model update(Long id, Long ownerId, ModelRequest request) {
        Model model = getForOwner(id, ownerId);
        applyRequest(model, request);
        return modelRepository.save(model);
    }

    @Transactional
    public void updatePhotoUrl(Long id, Long ownerId, String photoUrl) {
        Model model = getForOwner(id, ownerId);
        model.setPhotoUrl(photoUrl);
        modelRepository.save(model);
    }

    @Transactional
    public void delete(Long id, Long ownerId) {
        modelRepository.delete(getForOwner(id, ownerId));
    }

    private void applyRequest(Model model, ModelRequest request) {
        model.setName(request.name());
        model.setModelYear(request.modelYear());
        model.setScale(request.scale());
        model.setColor(request.color());
        model.setCondition(request.condition());
        model.setSeriesNumber(request.seriesNumber());
        model.setChase(request.chase());
        model.setPurchasePrice(request.purchasePrice());
        model.setPurchaseDate(request.purchaseDate());
        model.setPurchasedFrom(request.purchasedFrom());
        model.setNotes(request.notes());
        model.setAutomaker(request.automakerId() != null ? new Automaker(request.automakerId()) : null);
        model.setBrand(request.brandId() != null ? new Brand(request.brandId()) : null);
        model.setSeries(request.seriesId() != null ? new Series(request.seriesId()) : null);
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Model " + id + " not found");
    }
}
