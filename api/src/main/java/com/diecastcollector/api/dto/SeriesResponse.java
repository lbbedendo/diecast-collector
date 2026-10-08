package com.diecastcollector.api.dto;

import com.diecastcollector.api.domain.Series;

public record SeriesResponse(Long id, String name, Integer year, BrandResponse brand) {
    public static SeriesResponse from(Series series) {
        return new SeriesResponse(
                series.getId(),
                series.getName(),
                series.getYear(),
                // null when series is a bare id reference (e.g. on a Model's create/update response)
                series.getBrand() != null ? BrandResponse.from(series.getBrand()) : null);
    }
}
