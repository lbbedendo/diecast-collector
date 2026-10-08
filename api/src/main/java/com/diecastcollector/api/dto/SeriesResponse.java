package com.diecastcollector.api.dto;

import com.diecastcollector.api.domain.Series;

public record SeriesResponse(Long id, String name, Integer year) {
    public static SeriesResponse from(Series series) {
        return new SeriesResponse(series.getId(), series.getName(), series.getYear());
    }
}
