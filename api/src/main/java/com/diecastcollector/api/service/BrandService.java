package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Brand;
import com.diecastcollector.api.dto.BrandRequest;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.BrandRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> findAll() {
        return brandRepository.findAllByOrderByNameAsc();
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Brand create(BrandRequest request) {
        return brandRepository.save(new Brand(request.name()));
    }

    @Transactional
    public Brand update(Long id, BrandRequest request) {
        Brand brand = getById(id);
        brand.setName(request.name());
        return brandRepository.save(brand);
    }

    @Transactional
    public void delete(Long id) {
        brandRepository.delete(getById(id));
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Brand " + id + " not found");
    }
}
