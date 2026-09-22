package com.diecastcollector.api.controller;

import com.diecastcollector.api.dto.CollectionRequest;
import com.diecastcollector.api.dto.CollectionResponse;
import com.diecastcollector.api.service.CollectionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<CollectionResponse> getAll() {
        return collectionService.findAll().stream().map(CollectionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CollectionResponse getById(@PathVariable Long id) {
        return CollectionResponse.from(collectionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> create(@Valid @RequestBody CollectionRequest request) {
        var collection = collectionService.create(request);
        return ResponseEntity.created(URI.create("/collections/" + collection.getId()))
                .body(CollectionResponse.from(collection));
    }

    @PutMapping("/{id}")
    public CollectionResponse update(@PathVariable Long id, @Valid @RequestBody CollectionRequest request) {
        return CollectionResponse.from(collectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
