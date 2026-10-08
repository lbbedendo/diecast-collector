package com.diecastcollector.api.controller;

import com.diecastcollector.api.dto.SeriesRequest;
import com.diecastcollector.api.dto.SeriesResponse;
import com.diecastcollector.api.service.SeriesService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @GetMapping
    public List<SeriesResponse> getAll() {
        return seriesService.findAll().stream().map(SeriesResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SeriesResponse getById(@PathVariable Long id) {
        return SeriesResponse.from(seriesService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SeriesResponse> create(@Valid @RequestBody SeriesRequest request) {
        var series = seriesService.create(request);
        return ResponseEntity.created(URI.create("/series/" + series.getId()))
                .body(SeriesResponse.from(series));
    }

    @PutMapping("/{id}")
    public SeriesResponse update(@PathVariable Long id, @Valid @RequestBody SeriesRequest request) {
        return SeriesResponse.from(seriesService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seriesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
