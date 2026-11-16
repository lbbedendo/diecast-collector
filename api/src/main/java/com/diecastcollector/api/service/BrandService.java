package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Brand;
import com.diecastcollector.api.dto.BrandRequest;
import com.diecastcollector.api.exception.ConflictException;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.BrandRepository;
import com.diecastcollector.api.repository.SeriesRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final SeriesRepository seriesRepository;

    public BrandService(BrandRepository brandRepository, SeriesRepository seriesRepository) {
        this.brandRepository = brandRepository;
        this.seriesRepository = seriesRepository;
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
        Brand brand = getById(id);
        if (seriesRepository.existsByBrandId(id)) {
            throw new ConflictException("Brand " + id + " has Series and can't be deleted");
        }
        brandRepository.delete(brand);
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Brand " + id + " not found");
    }
}
