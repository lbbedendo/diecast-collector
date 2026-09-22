package com.diecastcollector.api.controller;

import com.diecastcollector.api.dto.ModelRequest;
import com.diecastcollector.api.dto.ModelResponse;
import com.diecastcollector.api.dto.PhotoUploadResponse;
import com.diecastcollector.api.security.CurrentUser;
import com.diecastcollector.api.service.ModelService;
import com.diecastcollector.api.service.PhotoStorageService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/models")
public class ModelController {

    private final ModelService modelService;
    private final PhotoStorageService photoStorageService;
    private final CurrentUser currentUser;

    public ModelController(ModelService modelService, PhotoStorageService photoStorageService, CurrentUser currentUser) {
        this.modelService = modelService;
        this.photoStorageService = photoStorageService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ModelResponse> getAll() {
        return modelService.findAllForOwner(currentUser.id()).stream().map(ModelResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ModelResponse getById(@PathVariable Long id) {
        return ModelResponse.from(modelService.getForOwner(id, currentUser.id()));
    }

    @PostMapping
    public ResponseEntity<ModelResponse> create(@Valid @RequestBody ModelRequest request) {
        var model = modelService.create(currentUser.id(), request);
        return ResponseEntity.created(URI.create("/models/" + model.getId())).body(ModelResponse.from(model));
    }

    @PutMapping("/{id}")
    public ModelResponse update(@PathVariable Long id, @Valid @RequestBody ModelRequest request) {
        return ModelResponse.from(modelService.update(id, currentUser.id(), request));
    }

    @PostMapping(value = "/{id}/photo", consumes = "multipart/form-data")
    public PhotoUploadResponse uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        // Fails with 404 first if this model isn't (or isn't yet) owned by the caller.
        modelService.getForOwner(id, currentUser.id());
        String photoUrl = photoStorageService.store(file);
        modelService.updatePhotoUrl(id, currentUser.id(), photoUrl);
        return new PhotoUploadResponse(photoUrl);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        modelService.delete(id, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
