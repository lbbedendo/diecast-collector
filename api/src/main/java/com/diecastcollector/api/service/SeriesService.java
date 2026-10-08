package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Brand;
import com.diecastcollector.api.domain.Series;
import com.diecastcollector.api.dto.SeriesRequest;
import com.diecastcollector.api.exception.ConflictException;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.SeriesRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final BrandService brandService;

    public SeriesService(SeriesRepository seriesRepository, BrandService brandService) {
        this.seriesRepository = seriesRepository;
        this.brandService = brandService;
    }

    public List<Series> findAll() {
        return seriesRepository.findAllByOrderByNameAsc();
    }

    public Series getById(Long id) {
        return seriesRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Series create(SeriesRequest request) {
        Brand brand = brandService.getById(request.brandId());
        ensureUnique(brand, request, null);
        return seriesRepository.save(new Series(brand, request.name(), request.year()));
    }

    @Transactional
    public Series update(Long id, SeriesRequest request) {
        Series series = getById(id);
        Brand brand = brandService.getById(request.brandId());
        ensureUnique(brand, request, id);
        series.setBrand(brand);
        series.setName(request.name());
        series.setYear(request.year());
        return seriesRepository.save(series);
    }

    @Transactional
    public void delete(Long id) {
        seriesRepository.delete(getById(id));
    }

    /**
     * A Series is identified by Brand + name + year (see CONTEXT.md). The DB constraint backs this
     * up, but checking here turns a duplicate into a 409 instead of a constraint-violation 500.
     */
    private void ensureUnique(Brand brand, SeriesRequest request, Long selfId) {
        Optional<Series> existing = request.year() != null
                ? seriesRepository.findByBrandIdAndNameAndYear(brand.getId(), request.name(), request.year())
                : seriesRepository.findByBrandIdAndNameAndYearIsNull(brand.getId(), request.name());
        if (existing.isPresent() && !existing.get().getId().equals(selfId)) {
            throw new ConflictException("Series '" + request.name() + "'"
                    + (request.year() != null ? " (" + request.year() + ")" : "")
                    + " already exists for brand " + brand.getName());
        }
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Series " + id + " not found");
    }
}
