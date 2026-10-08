package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Series;
import com.diecastcollector.api.dto.SeriesRequest;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.SeriesRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;

    public SeriesService(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    public List<Series> findAll() {
        return seriesRepository.findAllByOrderByNameAsc();
    }

    public Series getById(Long id) {
        return seriesRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Series create(SeriesRequest request) {
        return seriesRepository.save(new Series(request.name(), request.year()));
    }

    @Transactional
    public Series update(Long id, SeriesRequest request) {
        Series series = getById(id);
        series.setName(request.name());
        series.setYear(request.year());
        return seriesRepository.save(series);
    }

    @Transactional
    public void delete(Long id) {
        seriesRepository.delete(getById(id));
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Series " + id + " not found");
    }
}
