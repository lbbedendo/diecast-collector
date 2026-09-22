package com.diecastcollector.api.controller;

import com.diecastcollector.api.dto.BrandRequest;
import com.diecastcollector.api.dto.BrandResponse;
import com.diecastcollector.api.service.BrandService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public List<BrandResponse> getAll() {
        return brandService.findAll().stream().map(BrandResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BrandResponse getById(@PathVariable Long id) {
        return BrandResponse.from(brandService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        var brand = brandService.create(request);
        return ResponseEntity.created(URI.create("/brands/" + brand.getId())).body(BrandResponse.from(brand));
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return BrandResponse.from(brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
